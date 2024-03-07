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
import com.displee.compress.type.Compressors
import com.displee.io.Buffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.Whirlpool
import com.displee.util.createFileIfNotExists
import com.displee.util.generateWhirlpool
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.*

open class CacheLibrary(
    val path: Path,
    val clearDataAfterUpdate: Boolean = false,
    private val listener: ProgressListener? = null
) : AutoCloseable {

    constructor(
        path: String,
        clearDataAfterUpdate: Boolean = false,
        listener: ProgressListener? = null
    ) : this(Paths.get(path), clearDataAfterUpdate, listener)

    val mainFilePath: Path by lazy(LazyThreadSafetyMode.NONE) { path.resolve(MAIN_FILE_FILE_NAME) }
    val mainFile317Path: Path by lazy(LazyThreadSafetyMode.NONE) { path.resolve(MAIN_FILE_317_FILE_NAME) }

    val index255Path: Path by lazy(LazyThreadSafetyMode.NONE) { path.resolve(INDEX_255_FILE_NAME) }

    fun indexPath(indexId: Int): Path = path.resolve("$INDEX_FILE_NAME_PREFIX$indexId")

    lateinit var mainFile: RandomAccessFile

    private val indices: SortedMap<Int, Index> = TreeMap()
    val compressors = Compressors()
    val whirlpool = Whirlpool()
    var index255: Index255? = null
    private var rs3 = false

    var closed = false

    private val indexCount: Int
        get() = indices.keys.max() ?: 0

    init {
        init()
    }

    private fun init() {
        if (mainFile317Path.exists() && index255Path.notExists()) {
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
        mainFile = if (mainFilePath.exists()) {
            RandomAccessFile(mainFilePath.toFile(), "rw")
        } else {
            listener?.notify(-1.0, "Error, main file could not be found")
            throw FileNotFoundException("File[path=${mainFilePath.absolutePathString()}] could not be found.")
        }

        if (index255Path.notExists()) {
            listener?.notify(-1.0, "Error, checksum file could not be found.")
            throw FileNotFoundException("File[path=${index255Path.absolutePathString()}] could not be found.")
        }

        val index255 = Index255(this, RandomAccessFile(index255Path.toFile(), "rw"))
        this.index255 = index255

        listener?.notify(0.0, "Reading indices...")

        val indicesLength = index255.raf.length().toInt() / INDEX_SIZE
        rs3 = indicesLength > 39

        for (i in 0 until indicesLength) {
            val indexPath = indexPath(i)
            val progress = i / (indicesLength - 1.0)

            if (indexPath.notExists()) {
                listener?.notify(progress, "Could not load index $i, missing idx file.")
                continue
            }

            try {
                indices[i] = Index(this, i, RandomAccessFile(indexPath.toFile(), "rw"))
                listener?.notify(progress, "Loaded index $i.")
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.notify(progress, "Failed to load index $i.")
            }
        }
    }

    @Throws(IOException::class)
    private fun load317() {
        mainFile = if (mainFile317Path.exists()) {
            RandomAccessFile(mainFile317Path.toFile(), "rw")
        } else {
            listener?.notify(-1.0, "Error, main file could not be found")
            throw FileNotFoundException("File[path=${mainFile317Path.absolutePathString()}] could not be found.")
        }

        val indexFiles = path.toFile().listFiles { _: File, name: String ->
            return@listFiles name.startsWith(INDEX_FILE_NAME_PREFIX)
        }
        check(indexFiles != null) { "Files are null. Check your cache path." }

        listener?.notify(0.0, "Reading indices...")

        for (i in indexFiles.indices) {
            val indexPath = indexPath(i)
            if (indexPath.notExists()) continue

            val progress = i / (indexFiles.size - 1.0)
            try {
                indices[i] = Index317(this, i, RandomAccessFile(indexPath.toFile(), "rw"))
                listener?.notify(progress, "Loaded index $i .")
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.notify(progress, "Failed to load index $i.")
            }
        }
    }

    @JvmOverloads
    fun createIndex(
        compressionType: CompressionType = CompressionType.GZIP, version: Int = 6, revision: Int = 0,
        named: Boolean = false, whirlpool: Boolean = false, flag4: Boolean = false, flag8: Boolean = false,
        writeReferenceTable: Boolean = true, id: Int = if (indices.isEmpty()) 0 else indexCount + 1
    ): Index {
        val raf = RandomAccessFile(indexPath(id).toFile(), "rw")
        val index = (if (is317())
            Index317(this, id, raf)
        else Index(this, id, raf))
            .also { indices[id] = it }
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
        return createIndex(
            index.compressionType, index.version, index.revision,
            index.isNamed(), index.hasWhirlpool(), index.hasFlag4(), index.hasFlag8(), writeReferenceTable, index.id
        )
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
    fun put(
        index: Int,
        archive: Int,
        file: Int,
        data: ByteArray,
        xtea: IntArray? = null
    ): com.displee.cache.index.archive.file.File {
        return index(index).add(archive, xtea = xtea).add(file, data)
    }

    @JvmOverloads
    fun put(index: Int, archive: Int, data: ByteArray, xtea: IntArray? = null): Archive {
        val currentArchive = index(index).add(archive, -1, xtea)
        currentArchive.add(0, data)
        return currentArchive
    }

    @JvmOverloads
    fun put(
        index: Int,
        archive: Int,
        file: String,
        data: ByteArray,
        xtea: IntArray? = null
    ): com.displee.cache.index.archive.file.File {
        return index(index).add(archive, xtea = xtea).add(file, data)
    }

    @JvmOverloads
    fun put(index: Int, archive: String, data: ByteArray, xtea: IntArray? = null): Archive {
        val currentArchive = index(index).add(archive, xtea = xtea)
        currentArchive.add(0, data)
        return currentArchive
    }

    @JvmOverloads
    fun put(
        index: Int,
        archive: String,
        file: String,
        data: ByteArray,
        xtea: IntArray? = null
    ): com.displee.cache.index.archive.file.File {
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

        val id = indexCount - 1
        val index = indices[id] ?: return
        index.close()

        val indexPath = indexPath(id)
        val indexPathExists = indexPath.exists()
        if (!indexPathExists || !indexPath.deleteIfExists()) {
            throw RuntimeException("Failed to remove the random access file of the argued index[id=$id, file exists=$indexPathExists]")
        }

        index255?.raf?.setLength(id * INDEX_SIZE.toLong())
        indices.remove(id)
    }

    @JvmOverloads
    fun generateUkeys(
        writeWhirlpool: Boolean = true,
        exponent: BigInteger? = null,
        modulus: BigInteger? = null
    ): ByteArray {
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

    fun rebuild(directory: Path) {
        directory.createDirectories()
        if (is317()) {
            mainFile317Path.createFileIfNotExists()
        } else {
            index255Path.createFileIfNotExists()
            mainFilePath.createFileIfNotExists()
        }

        val newLibrary = CacheLibrary(directory)

        val indicesSize = indices.values.size

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

    fun rebuild(directory: File) = rebuild(directory.toPath())

    fun fixCrcs(update: Boolean) {
        indices.values.forEach {
            if (it.archiveIds().isEmpty()) {
                return@forEach
            }
            it.fixCRCs(update)
        }
    }

    override fun close() {
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
        return index.revision >= 300 && indexCount <= 23
    }

    fun isRS3(): Boolean {
        return rs3
    }

    fun indices(): Array<Index> {
        return indices.values.toTypedArray()
    }

    companion object {

        const val CACHE_FILE_NAME = "main_file_cache"

        const val MAIN_FILE_FILE_NAME = "$CACHE_FILE_NAME.dat2"
        const val MAIN_FILE_317_FILE_NAME = "$CACHE_FILE_NAME.dat"

        const val INDEX_FILE_NAME_PREFIX = "$CACHE_FILE_NAME.idx"

        const val INDEX_255_FILE_NAME = "${INDEX_FILE_NAME_PREFIX}255"

        @JvmStatic
        @JvmOverloads
        fun create(
            path: Path,
            clearDataAfterUpdate: Boolean = false,
            listener: ProgressListener? = null
        ): CacheLibrary = CacheLibrary(path, clearDataAfterUpdate, listener)

        @JvmStatic
        @JvmOverloads
        fun create(
            path: String,
            clearDataAfterUpdate: Boolean = false,
            listener: ProgressListener? = null
        ): CacheLibrary = CacheLibrary(path, clearDataAfterUpdate, listener)

    }

}
