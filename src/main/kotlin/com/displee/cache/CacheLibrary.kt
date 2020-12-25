package com.displee.cache

import com.displee.cache.index.Index
import com.displee.cache.index.Index.Companion.INDEX_SIZE
import com.displee.cache.index.Index.Companion.WHIRLPOOL_SIZE
import com.displee.cache.index.Index255
import com.displee.cache.index.Index317
import com.displee.cache.index.ReferenceTable.Companion.FLAG_4
import com.displee.cache.index.ReferenceTable.Companion.FLAG_8
import com.displee.cache.index.ReferenceTable.Companion.FLAG_NAME
import com.displee.cache.index.ReferenceTable.Companion.FLAG_WHIRLPOOL
import com.displee.cache.index.archive.Archive
import com.displee.compress.CompressionType
import com.displee.io.Buffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.generateWhirlpool
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.util.*

open class CacheLibrary(val path: String, val clearDataAfterUpdate: Boolean = false, private val listener: ProgressListener? = null) {

    lateinit var mainFile: RandomAccessFile

    private val indices: SortedMap<Int, Index> = TreeMap<Int, Index>()
    var index255: Index255? = null
    private var rs3 = false

    var closed = false

    init {
        init()
    }

    private fun init() {
        val mainFile317 = File(path, "$CACHE_FILE_NAME.dat")
        val index255 = File(path, "$CACHE_FILE_NAME.idx255")
        if (mainFile317.exists() && !index255.exists()) {
            load317()
        } else {
            load()
        }
    }

    /**
     * Re-create indices and re-read reference tables.
     */
    fun reload() {
        indices.clear()
        init()
    }

    @Throws(IOException::class)
    private fun load() {
        val main = File(path, "$CACHE_FILE_NAME.dat2")
        mainFile = if (main.exists()) {
            RandomAccessFile(main, "rw")
        } else {
            listener?.notify(-1.0, "Error, main file could not be found")
            throw FileNotFoundException("File[path=${main.absolutePath}] could not be found.")
        }
        val index255File = File(path, "$CACHE_FILE_NAME.idx255")
        if (!index255File.exists()) {
            listener?.notify(-1.0, "Error, checksum file could not be found.")
            throw FileNotFoundException("File[path=${index255File.absolutePath}] could not be found.")
        }
        val index255 = Index255(this, RandomAccessFile(index255File, "rw"))
        this.index255 = index255
        listener?.notify(0.0, "Reading indices...")
        val indicesLength = index255.raf.length().toInt() / INDEX_SIZE
        rs3 = indicesLength > 39
        for (i in 0 until indicesLength) {
            val file = File(path, "$CACHE_FILE_NAME.idx$i")
            val progress = i / (indicesLength - 1.0)
            if (!file.exists()) {
                listener?.notify(progress, "Could not load index $i, missing idx file.")
                continue
            }
            try {
                indices[i] = Index(this, i, RandomAccessFile(file, "rw"))
                listener?.notify(progress, "Loaded index $i.")
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.notify(progress, "Failed to load index $i.")
            }
        }
    }

    @Throws(IOException::class)
    private fun load317() {
        val main = File(path, "$CACHE_FILE_NAME.dat")
        mainFile = if (main.exists()) {
            RandomAccessFile(main, "rw")
        } else {
            listener?.notify(-1.0, "Error, main file could not be found")
            throw FileNotFoundException("File[path=${main.absolutePath}] could not be found.")
        }
        val indexFiles = File(path).listFiles { _: File, name: String ->
            return@listFiles name.startsWith("$CACHE_FILE_NAME.idx")
        }
        check(indexFiles != null) { "Files are null. Check your cache path." }
        listener?.notify(0.0, "Reading indices...")
        for (i in indexFiles.indices) {
            val file = File(path, "$CACHE_FILE_NAME.idx$i")
            val progress = i / (indexFiles.size - 1.0)
            if (!file.exists()) {
                continue
            }
            try {
                indices[i] = Index317(this, i, RandomAccessFile(file, "rw"))
                listener?.notify(progress, "Loaded index $i .")
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.notify(progress, "Failed to load index $i.")
            }
        }
    }

    @JvmOverloads
    fun createIndex(compressionType: CompressionType = CompressionType.GZIP, version: Int = 6, revision: Int = 0,
                    named: Boolean = false, whirlpool: Boolean = false, flag4: Boolean = false, flag8: Boolean = false,
                    writeReferenceTable: Boolean = true): Index {
        val id = indices.size
        val raf = RandomAccessFile(File(path, "$CACHE_FILE_NAME.idx$id"), "rw")
        val index = (if (is317()) Index317(this, id, raf) else Index(this, id, raf)).also { indices[id] = it }
        if (!writeReferenceTable) {
            return index
        }
        index.version = version
        index.revision = revision
        index.compressionType = compressionType
        if (named) {
            index.flagMask(FLAG_NAME)
        }
        if (whirlpool) {
            index.flagMask(FLAG_WHIRLPOOL)
        }
        if (isRS3()) {
            if (flag4) {
                index.flagMask(FLAG_4)
            }
            if (flag8) {
                index.flagMask(FLAG_8)
            }
        }
        index.flag()
        check(index.update())
        return index
    }

    fun createIndex(index: Index, writeReferenceTable: Boolean = true): Index {
        return createIndex(index.compressionType, index.version, index.revision,
                index.isNamed(), index.hasWhirlpool(), index.hasFlag4(), index.hasFlag8(), writeReferenceTable)
    }

    fun exists(id: Int): Boolean {
        return indices.containsKey(id)
    }

    fun index(id: Int): Index {
        val index = indices[id]
        checkNotNull(index) { "Index $id doesn't exist. Please use the {@link exists(int) exists} function to verify whether an index exists." }
        return index
    }

    @JvmOverloads
    fun put(index: Int, archive: Int, file: Int, data: ByteArray, xtea: IntArray? = null): com.displee.cache.index.archive.file.File {
        return index(index).add(archive, xtea = xtea).add(file, data)
    }

    @JvmOverloads
    fun put(index: Int, archive: Int, data: ByteArray, xtea: IntArray? = null): Archive {
        val currentArchive = index(index).add(archive, -1, xtea)
        currentArchive.add(0, data)
        return currentArchive
    }

    @JvmOverloads
    fun put(index: Int, archive: Int, file: String, data: ByteArray, xtea: IntArray? = null): com.displee.cache.index.archive.file.File {
        return index(index).add(archive, xtea = xtea).add(file, data)
    }

    @JvmOverloads
    fun put(index: Int, archive: String, data: ByteArray, xtea: IntArray? = null): Archive {
        val currentArchive = index(index).add(archive, xtea = xtea)
        currentArchive.add(0, data)
        return currentArchive
    }

    @JvmOverloads
    fun put(index: Int, archive: String, file: String, data: ByteArray, xtea: IntArray? = null): com.displee.cache.index.archive.file.File {
        return index(index).add(archive, xtea = xtea).add(file, data)
    }

    @JvmOverloads
    fun data(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray? {
        return index(index).archive(archive, xtea)?.file(file)?.data
    }

    @JvmOverloads
    fun data(index: Int, archive: Int, file: String, xtea: IntArray? = null): ByteArray? {
        return index(index).archive(archive, xtea)?.file(file)?.data
    }

    @JvmOverloads
    fun data(index: Int, archive: String, file: Int, xtea: IntArray? = null): ByteArray? {
        return index(index).archive(archive, xtea)?.file(file)?.data
    }

    @JvmOverloads
    fun data(index: Int, archive: String, file: String, xtea: IntArray? = null): ByteArray? {
        return index(index).archive(archive, xtea)?.file(file)?.data
    }

    @JvmOverloads
    fun data(index: Int, archive: String, xtea: IntArray? = null): ByteArray? {
        return data(index, archive, 0, xtea)
    }

    fun remove(index: Int, archive: Int, file: Int): com.displee.cache.index.archive.file.File? {
        return index(index).archive(archive)?.remove(file)
    }

    fun remove(index: Int, archive: Int, file: String): com.displee.cache.index.archive.file.File? {
        return index(index).archive(archive)?.remove(file)
    }

    fun remove(index: Int, archive: String, file: String): com.displee.cache.index.archive.file.File? {
        return index(index).archive(archive)?.remove(file)
    }

    fun remove(index: Int, archive: Int): Archive? {
        return index(index).remove(archive)
    }

    fun remove(index: Int, archive: String): Archive? {
        return index(index).remove(archive)
    }

    fun update() {
        for (index in indices.values) {
            if (index.flaggedArchives().isEmpty() && !index.flagged()) {
                continue
            }
            index.update()
        }
    }

    @Throws(RuntimeException::class)
    fun deleteLastIndex() {
        if (is317()) {
            throw UnsupportedOperationException("317 not supported to remove indices yet.")
        }
        val id = indices.size - 1
        val index = indices[id] ?: return
        index.close()
        val file = File(path, "$CACHE_FILE_NAME.idx$id")
        if (!file.exists() || !file.delete()) {
            throw RuntimeException("Failed to remove the random access file of the argued index[id=$id, file exists=${file.exists()}]")
        }
        index255?.raf?.setLength(id * INDEX_SIZE.toLong())
        indices.remove(id)
    }

    fun generateOldUkeys(): ByteArray {
        val buffer = OutputBuffer(indices.size * 8)
        for (index in indices()) {
            buffer.writeInt(index.crc)
            buffer.writeInt(index.revision)
        }
        return buffer.array()
    }

    fun generateNewUkeys(exponent: BigInteger, modulus: BigInteger): ByteArray {
        val buffer = OutputBuffer(6 + indices.size * 72)
        buffer.offset = 5
        buffer.writeByte(indices.size)
        val emptyWhirlpool = ByteArray(WHIRLPOOL_SIZE)
        for (index in indices()) {
            buffer.writeInt(index.crc).writeInt(index.revision).writeBytes(index.whirlpool ?: emptyWhirlpool)
        }
        val indexArray = buffer.array()
        val whirlpoolBuffer = OutputBuffer(WHIRLPOOL_SIZE + 1).writeByte(1).writeBytes(indexArray.generateWhirlpool(5, indexArray.size - 5))
        buffer.writeBytes(Buffer.cryptRSA(whirlpoolBuffer.array(), exponent, modulus))
        val end = buffer.offset
        buffer.offset = 0
        buffer.writeByte(0)
        buffer.writeInt(end - 5)
        buffer.offset = end
        return buffer.array()
    }

    fun rebuild(directory: File) {
        File(directory.path).mkdirs()
        if (is317()) {
            File(directory.path, "$CACHE_FILE_NAME.dat").createNewFile()
        } else {
            File(directory.path, "$CACHE_FILE_NAME.idx255").createNewFile()
            File(directory.path, "$CACHE_FILE_NAME.dat2").createNewFile()
        }
        val indicesSize = indices.values.size
        val newLibrary = CacheLibrary(directory.path)
        for (index in indices.values) {
            val id = index.id
            print("\rBuilding index $id/$indicesSize...")
            val archiveSector = index255?.readArchiveSector(id)
            var writeReferenceTable = true
            if (!is317() && archiveSector == null) { //some empty indices don't even have a reference table
                writeReferenceTable = false //in that case, don't write it
            }
            val newIndex = newLibrary.createIndex(index, writeReferenceTable)
            for (i in index.archiveIds()) { //only write referenced archives
                val data = index.readArchiveSector(i)?.data ?: continue
                newIndex.writeArchiveSector(i, data)
            }
            if (archiveSector != null) {
                newLibrary.index255?.writeArchiveSector(id, archiveSector.data)
            }
        }
        newLibrary.close()
        println("\rFinished building $indicesSize indices.")
    }

    fun fixCrcs(update: Boolean) {
        indices.values.forEach {
            if (it.archiveIds().isEmpty()) {
                return@forEach
            }
            it.fixCRCs(update)
        }
    }

    fun close() {
        if (closed) {
            return
        }
        mainFile.close()
        index255?.close()
        indices.values.forEach { it.close() }
        closed = true
    }

    fun first(): Index? {
        if (indices.isEmpty()) {
            return null
        }
        return indices[indices.firstKey()]
    }

    fun last(): Index? {
        if (indices.isEmpty()) {
            return null
        }
        return indices[indices.lastKey()]
    }

    fun is317(): Boolean {
        return index255 == null
    }

    fun isOSRS(): Boolean {
        val index = index(2)
        return index.revision >= 300 && indices.size <= 23
    }

    fun isRS3(): Boolean {
        return rs3
    }

    fun indices(): Array<Index> {
        return indices.values.toTypedArray()
    }

    companion object {
        const val CACHE_FILE_NAME = "main_file_cache"
        
        @JvmStatic
        @JvmOverloads
        fun create(path: String, clearDataAfterUpdate: Boolean = false, listener: ProgressListener? = null): CacheLibrary {
            return CacheLibrary(path, clearDataAfterUpdate, listener)
        }
    }

}
