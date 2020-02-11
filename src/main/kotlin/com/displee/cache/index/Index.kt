package com.displee.cache.index

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.displee.cache.index.archive.Archive
import com.displee.cache.index.archive.ArchiveSector
import com.displee.compress.CompressionType
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.CRCHash
import com.displee.util.Compression
import com.displee.util.Utils
import com.displee.util.Whirlpool
import java.io.RandomAccessFile

open class Index(origin: CacheLibrary, id: Int, var randomAccessFile: RandomAccessFile) : ReferenceTable(origin, id) {

    var crc = 0
    var whirlpool: ByteArray? = null
    var compressionType: CompressionType = CompressionType.NONE
    private var cached = false
    protected var closed = false

    val info: String get() = "Index[id=" + id + ", archives=" + archives.size + ", compression=" + compressionType + "]"

    init {
        init()
    }

    protected open fun init() {
        if (id < 0 || id >= 255) {
            return
        }
        val archiveSector = origin.checksumTable?.readArchiveSector(id) ?: return
        val archiveSectorData = archiveSector.data
        crc = Utils.generateCrc(archiveSectorData)
        whirlpool = Whirlpool.generate(archiveSectorData, 0, archiveSectorData.size)
        read(InputBuffer(Compression.decompress(archiveSector)))
        compressionType = archiveSector.compressionType
    }

    fun cache() {
        cache(emptyMap())
    }

    fun cache(xteas: Map<Int, IntArray> = emptyMap()) {
        check(!closed) { "Index is closed." }
        if (cached) {
            return
        }
        for (archiveId in archiveIds()) {
            try {
                archive(archiveId, xteas[archiveId], false)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        cached = true
    }

    fun unCache() {
        for (archive in archives()) {
            archive.restore()
        }
        cached = false
    }

    @JvmOverloads
    open fun update(xteas: Map<Int, IntArray> = emptyMap(), listener: ProgressListener? = null): Boolean {
        check(!closed) { "Index is closed." }
        var updateCount = 0
        val archives = archives()
        archives.forEach { if (it.flagged()) updateCount++ }
        var i = 0.0
        archives.forEach {
            if (!it.flagged()) {
                return@forEach
            }
            i++
            it.unFlag()
            listener?.notify(i / updateCount * 80.0, "Repacking archive ${it.id}...")
            val compressed = Compression.compress(it.write(), compressionType, xteas[it.id], it.revision)
            it.crc = CRCHash.generate(compressed, 0, compressed.size - 2)
            it.whirlpool = Whirlpool.generate(compressed, 0, compressed.size - 2)
            val written = writeArchiveSector(it.id, compressed)
            check(written) { "Unable to write data to archive sector. Your cache may be corrupt." }
            if (origin.clearDataAfterUpdate) {
                it.restore()
            }
        }
        listener?.notify(85.0, "Updating checksum table for index $id...")
        if (updateCount != 0 && !flagged()) {
            flag()
        }
        if (flagged()) {
            val indexData = Compression.compress(write(), compressionType)
            crc = Utils.generateCrc(indexData)
            whirlpool = Whirlpool.generate(indexData, 0, indexData.size)
            val written = origin.checksumTable?.writeArchiveSector(this.id, indexData) ?: false
            check(written) { "Unable to write data to checksum table. Your cache may be corrupt." }
        }
        listener?.notify(100.0, "Successfully updated index $id.")
        return true
    }

    fun readArchiveSector(id: Int): ArchiveSector? {
        check(!closed) { "Index is closed." }
        synchronized(origin.mainFile) {
            try {
                if (origin.mainFile.length() < INDEX_SIZE * id + INDEX_SIZE) {
                    return null
                }
                val sectorData = ByteArray(SECTOR_SIZE)
                randomAccessFile.seek(id.toLong() * INDEX_SIZE)
                randomAccessFile.read(sectorData, 0, INDEX_SIZE)
                val buffer = InputBuffer(sectorData)
                val archiveSector = ArchiveSector(id > 65535, buffer.read24BitInt(), buffer.read24BitInt())
                if (archiveSector.size < 0 || archiveSector.position <= 0 || archiveSector.position > origin.mainFile.length() / SECTOR_SIZE) {
                    return null
                }
                var read = 0
                var chunk = 0
                val sectorHeaderSize = SECTOR_HEADER_SIZE + if (id > 65535) 2 else 0
                val sectorDataSize = SECTOR_DATA_SIZE - if (id > 65535) 2 else 0
                while (read < archiveSector.size) {
                    if (archiveSector.position == 0) {
                        return null
                    }
                    var requiredToRead = archiveSector.size - read
                    if (requiredToRead > sectorDataSize) {
                        requiredToRead = sectorDataSize
                    }
                    origin.mainFile.seek(archiveSector.position.toLong() * SECTOR_SIZE)
                    origin.mainFile.read(buffer.raw(), 0, requiredToRead + sectorHeaderSize)
                    buffer.offset = 0
                    archiveSector.read(buffer)
                    if (!isIndexValid(archiveSector.index) || id != archiveSector.id || chunk != archiveSector.chunk) {
                        throw RuntimeException("Error, the read data is incorrect. Data[currentIndex=" + this.id + ", index=" + archiveSector.index + ", currentId=" + id + ", id=" + archiveSector.id + ", currentChunk=" + chunk + ", chunk=" + archiveSector.chunk + "]")
                    } else if (archiveSector.nextPosition < 0 || archiveSector.nextPosition > origin.mainFile.length() / SECTOR_SIZE) {
                        throw RuntimeException("Error, the next position is invalid.")
                    }
                    val bufferData = buffer.raw()
                    for (i in 0 until requiredToRead) {
                        archiveSector.data[read++] = bufferData[i + sectorHeaderSize]
                    }
                    archiveSector.position = archiveSector.nextPosition
                    chunk++
                }
                return archiveSector
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        return null
    }

    fun writeArchiveSector(id: Int, data: ByteArray): Boolean {
        check(!closed) { "Index is closed." }
        synchronized(origin.mainFile) {
            return try {
                var position: Int
                var archive: Archive? = null
                var archiveSector: ArchiveSector? = readArchiveSector(id)
                if (this.id != 255) {
                    archive = archive(id, null, true)
                }
                var overWrite = (this.id == 255 && archiveSector != null) || !(archive?.new ?: false)
                val sectorData = ByteArray(SECTOR_SIZE)
                if (overWrite) {
                    if (INDEX_SIZE * id + INDEX_SIZE > randomAccessFile.length()) {
                        return false
                    }
                    randomAccessFile.seek(id.toLong() * INDEX_SIZE)
                    randomAccessFile.read(sectorData, 0, INDEX_SIZE)
                    val buffer = InputBuffer(sectorData)
                    buffer.offset = 3
                    position = buffer.read24BitInt()
                    if (position <= 0 || position > origin.mainFile.length() / SECTOR_SIZE) {
                        return false
                    }
                } else {
                    position = ((origin.mainFile.length() + (SECTOR_SIZE - 1)) / SECTOR_SIZE).toInt()
                    if (position == 0) {
                        position = 1
                    }
                    archiveSector = ArchiveSector(id > 65535, data.size, position, id, indexToWrite(this.id))
                }
                archiveSector ?: return false
                val buffer = OutputBuffer(6)
                buffer.write24BitInt(data.size)
                buffer.write24BitInt(position)
                randomAccessFile.seek(id.toLong() * INDEX_SIZE)
                randomAccessFile.write(buffer.array(), 0, INDEX_SIZE)
                var written = 0
                var chunk = 0
                val archiveHeaderSize = SECTOR_HEADER_SIZE + if (id > 65535) 2 else 0
                val archiveDataSize = SECTOR_DATA_SIZE - if (id > 65535) 2 else 0
                while (written < data.size) {
                    var currentPosition = 0
                    if (overWrite) {
                        origin.mainFile.seek(position.toLong() * SECTOR_SIZE)
                        origin.mainFile.read(sectorData, 0, archiveHeaderSize)
                        archiveSector.read(InputBuffer(sectorData))
                        currentPosition = archiveSector.nextPosition
                        if (archiveSector.id != id || archiveSector.chunk != chunk || !isIndexValid(archiveSector.index)) {
                            return false
                        }
                        if (currentPosition < 0 || origin.mainFile.length() / SECTOR_SIZE < currentPosition) {
                            return false
                        }
                    }
                    if (currentPosition == 0) {
                        overWrite = false
                        currentPosition = ((origin.mainFile.length() + (SECTOR_SIZE - 1)) / SECTOR_SIZE).toInt()
                        if (currentPosition == 0) {
                            currentPosition++
                        }
                        if (currentPosition == position) {
                            currentPosition++
                        }
                    }
                    if (data.size - written <= archiveDataSize) {
                        currentPosition = 0
                    }
                    archiveSector.chunk = chunk
                    archiveSector.position = currentPosition
                    origin.mainFile.seek(position.toLong() * SECTOR_SIZE)
                    origin.mainFile.write(archiveSector.write(), 0, archiveHeaderSize)
                    var length = data.size - written
                    if (length > archiveDataSize) {
                        length = archiveDataSize
                    }
                    origin.mainFile.write(data, written, length)
                    written += length
                    position = currentPosition
                    chunk++
                }
                true
            } catch (t: Throwable) {
                t.printStackTrace()
                false
            }
        }
    }

    fun fixCRCs(update: Boolean) {
        check(!closed) { "Index is closed." }
        val archiveIds = archiveIds()
        var flag = false
        for (i in archiveIds) {
            val sector = readArchiveSector(i) ?: continue
            val correctCRC = CRCHash.generate(sector.data, 0, sector.data.size - 2)
            val archive = archive(i) ?: continue
            val currentCRC = archive.crc
            if (currentCRC == correctCRC) {
                continue
            }
            println("Incorrect CRC in index $id -> archive $i, current_crc=$currentCRC, correct_crc=$correctCRC")
            archive.flag()
            flag = true
        }
        val sectorData = origin.checksumTable?.readArchiveSector(id)?.data ?: return
        val indexCRC = Utils.generateCrc(sectorData)
        if (crc != indexCRC) {
            flag = true
        }
        if (flag && update) {
            update()
        } else if (!flag) {
            println("No invalid CRCs found.")
            return
        }
        unCache()
    }

    fun close() {
        if (closed) {
            return
        }
        randomAccessFile.close()
        closed = true
    }

    protected open fun isIndexValid(index: Int): Boolean {
        return this.id == index
    }

    protected open fun indexToWrite(index: Int): Int {
        return index
    }

    override fun toString(): String {
        return "Index $id"
    }

    companion object {

        const val INDEX_SIZE = 6
        const val SECTOR_HEADER_SIZE = 8
        const val SECTOR_DATA_SIZE = 512
        const val SECTOR_SIZE = SECTOR_HEADER_SIZE + SECTOR_DATA_SIZE
        const val WHIRLPOOL_SIZE = 64

    }

}