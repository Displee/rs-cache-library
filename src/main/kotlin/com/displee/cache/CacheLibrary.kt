package com.displee.cache

import com.displee.cache.index.Index
import com.displee.cache.index.Index.Companion.INDEX_SIZE
import com.displee.cache.index.Index.Companion.WHIRLPOOL_SIZE
import com.displee.cache.index.Index255
import com.displee.cache.index.Index317
import com.displee.cache.index.ReferenceTable.Companion.FLAG_LENGTHS
import com.displee.cache.index.ReferenceTable.Companion.FLAG_CHECKSUMS
import com.displee.cache.index.ReferenceTable.Companion.FLAG_NAME
import com.displee.cache.index.ReferenceTable.Companion.FLAG_WHIRLPOOL
import com.displee.cache.index.archive.Archive
import com.displee.compress.CompressionType
import com.displee.compress.type.Compressors
import com.displee.io.Buffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.Whirlpool
import com.displee.util.generateWhirlpool
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.util.*

open class CacheLibrary(val path: String, val clearDataAfterUpdate: Boolean = false, private val listener: ProgressListener? = null) {

    lateinit var mainFile: RandomAccessFile

    private var indices: Array<Index?> = arrayOfNulls(0)
    val compressors = Compressors()
    val whirlpool = Whirlpool()
    var index255: Index255? = null
    private var rs3 = false

    var closed = false

    private val indexCount: Int
        get() = indices.size

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
        indices = arrayOfNulls(0)
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
        indices = arrayOfNulls(indicesLength)
        rs3 = indicesLength > 39
        for (i in 0 until indicesLength) {
            val file = File(path, "$CACHE_FILE_NAME.idx$i")
            val progress = i / (indicesLength - 1.0)
            if (!file.exists()) {
                setIndex(i, null)
                listener?.notify(progress, "Could not load index $i, missing idx file.")
                continue
            }
            try {
                setIndex(i, Index(this, i, RandomAccessFile(file, "rw")))
                listener?.notify(progress, "Loaded index $i.")
            } catch (e: Exception) {
                setIndex(i, null)
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
                setIndex(i, null)
                continue
            }
            try {
                setIndex(i, Index317(this, i, RandomAccessFile(file, "rw")))
                listener?.notify(progress, "Loaded index $i .")
            } catch (e: Exception) {
                setIndex(i, null)
                e.printStackTrace()
                listener?.notify(progress, "Failed to load index $i.")
            }
        }
    }

    private fun setIndex(index: Int, value: Index?) {
        if (index >= indices.size) {
            indices = indices.copyOf(index + 1)
        }
        indices[index] = value
    }

    @JvmOverloads
    fun createIndex(compressionType: CompressionType = CompressionType.GZIP, version: Int = 6, revision: Int = 0,
                    named: Boolean = false, whirlpool: Boolean = false, lengths: Boolean = false, checksums: Boolean = false,
                    writeReferenceTable: Boolean = true, id: Int = if (indices.isEmpty()) 0 else indexCount + 1): Index {
        val raf = RandomAccessFile(File(path, "$CACHE_FILE_NAME.idx$id"), "rw")
        val index = (if (is317()) Index317(this, id, raf) else Index(this, id, raf)).also { setIndex(id, it) }
        if (!writeReferenceTable) {
            return index
        }
        index.version = version
        index.revision = revision
        index.compressionType = compressionType
        index.compressor = compressors.get(compressionType)
        if (named) {
            index.flagMask(FLAG_NAME)
        }
        if (whirlpool) {
            index.flagMask(FLAG_WHIRLPOOL)
        }
        if (lengths) {
            index.flagMask(FLAG_LENGTHS)
        }
        if (checksums) {
            index.flagMask(FLAG_CHECKSUMS)
        }
        index.flag()
        check(index.update())
        return index
    }

    fun createIndex(index: Index, writeReferenceTable: Boolean = true): Index {
        return createIndex(index.compressionType, index.version, index.revision,
                index.isNamed(), index.hasWhirlpool(), index.hasLengths(), index.hasChecksums(), writeReferenceTable, index.id)
    }

    fun exists(id: Int): Boolean {
        return indices.getOrNull(id) != null
    }

    fun index(id: Int): Index {
        val index = indices.getOrNull(id)
        return checkNotNull(index) { "Index $id doesn't exist. Please use the {@link exists(int) exists} function to verify whether an index exists." }
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
        return put(index, archive, 0, data, xtea)
    }

    @JvmOverloads
    fun put(index: Int, archive: String, file: Int, data: ByteArray, xtea: IntArray? = null): Archive {
        val currentArchive = index(index).add(archive, xtea = xtea)
        currentArchive.add(file, data)
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

    fun remove(index: Int, archive: String, file: Int): com.displee.cache.index.archive.file.File? {
        return index(index).archive(archive)?.remove(file)
    }

    fun remove(index: Int, archive: Int): Archive? {
        return index(index).remove(archive)
    }

    fun remove(index: Int, archive: String): Archive? {
        return index(index).remove(archive)
    }

    fun update() {
        for (index in indices) {
            if (index == null) {
                continue
            }
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
        val id = indexCount
        val index = indices.getOrNull(id) ?: return
        index.close()
        val file = File(path, "$CACHE_FILE_NAME.idx$id")
        if (!file.exists() || !file.delete()) {
            throw RuntimeException("Failed to remove the random access file of the argued index[id=$id, file exists=${file.exists()}]")
        }
        index255?.raf?.setLength(id * INDEX_SIZE.toLong())
        indices[id] = null
        indices = indices.copyOf(id)
    }

    @JvmOverloads
    fun generateUkeys(writeWhirlpool: Boolean = true, exponent: BigInteger? = null, modulus: BigInteger? = null): ByteArray {
        val buffer = OutputBuffer(6 + indexCount * 72)
        if (writeWhirlpool) {
            buffer.writeByte(indexCount)
        }
        val emptyWhirlpool = ByteArray(WHIRLPOOL_SIZE)
        for (index in indices()) {
            buffer.writeInt(index.crc).writeInt(index.revision)
            if (writeWhirlpool) {
                buffer.writeBytes(index.whirlpool ?: emptyWhirlpool)
            }
        }
        if (writeWhirlpool) {
            val indexData = buffer.array()
            val whirlpoolBuffer = OutputBuffer(WHIRLPOOL_SIZE + 1)
                .writeByte(0)
                .writeBytes(indexData.generateWhirlpool(whirlpool, 5, indexData.size - 5))
            if (exponent != null && modulus != null) {
                buffer.writeBytes(Buffer.cryptRSA(whirlpoolBuffer.array(), exponent, modulus))
            }
        }
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
        val indicesSize = indices.size
        val newLibrary = CacheLibrary(directory.path)
        for (index in indices) {
            if (index == null) {
                continue
            }
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
        for(index in indices) {
            if (index == null || index.archiveIds().isEmpty()) {
                continue
            }
            index.fixCRCs(update)
        }
    }

    fun close() {
        if (closed) {
            return
        }
        mainFile.close()
        index255?.close()
        for(index in indices) {
            index?.close()
        }
        closed = true
    }

    fun first(): Index? {
        if (indices.isEmpty()) {
            return null
        }
        return indices.getOrNull(0)
    }

    fun last(): Index? {
        if (indices.isEmpty()) {
            return null
        }
        return indices.getOrNull(indices.size - 1)
    }

    fun is317(): Boolean {
        return index255 == null
    }

    fun isOSRS(): Boolean {
        val index = index(2)
        return index.revision >= 300 && indexCount <= 23
    }

    fun isRS3(): Boolean {
        return rs3
    }

    fun indices(): Array<Index> {
        return indices.filterNotNull().toTypedArray()
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
