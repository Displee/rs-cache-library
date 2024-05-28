package com.displee.cache.index

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.displee.cache.index.archive.Archive
import com.displee.cache.index.archive.Archive317
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.generateCrc
import com.displee.util.generateWhirlpool
import com.displee.util.hashCode317
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class Index317(origin: CacheLibrary, id: Int, raf: RandomAccessFile) : Index(origin, id, raf) {

    override fun init() {
        val archiveLength = try {
            (raf.length() / INDEX_SIZE.toLong()).toInt()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        var versions: IntArray? = null
        var crcs: IntArray? = null
        if (id != CONFIG_INDEX && id < VERSION_FILES.size) {
            versions = readArchiveProperties(VERSION_FILES[id - 1], BufferType.SHORT)
            crcs = readArchiveProperties(CRC_FILES[id - 1], BufferType.INT)
        }
        for (i in 0 until archiveLength) {
            readArchiveSector(i) ?: continue
            val archive = Archive317(origin.compressors.bzip2, i)
            archives[i] = archive
            if (versions == null || crcs == null || i >= versions.size || i >= crcs.size) {
                continue
            }
            archive.revision = versions[i]
            archive.crc = crcs[i]
        }
    }

    override fun read(buffer: InputBuffer) {

    }

    override fun write(): ByteArray {
        return byteArrayOf()
    }

    override fun update(listener: ProgressListener?): Boolean {
        check(!closed) { "Index is closed." }
        val flaggedArchives = flaggedArchives()
        val archives = archives()
        var i = 0.0
        flaggedArchives.forEach {
            i++
            if (it.autoUpdateRevision) {
                it.revision++
            }
            it.unFlag()
            listener?.notify((i / flaggedArchives.size) * 0.80, "Repacking archive ${it.id}...")
            val compressed = it.write()
            it.crc = compressed.generateCrc()
            it.whirlpool = compressed.generateWhirlpool(origin.whirlpool)
            val written = writeArchiveSector(it.id, compressed)
            check(written) { "Unable to write data to archive sector. Your cache may be corrupt." }
            if (origin.clearDataAfterUpdate) {
                it.restore()
            }
        }
        listener?.notify(0.85, "Updating version archives for index $id...")
        if (flaggedArchives.isNotEmpty() && !flagged()) {
            flag()
        }
        if (id != CONFIG_INDEX && flagged()) {
            writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::revision).toArray(), VERSION_FILES[id - 1], BufferType.SHORT)
            writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::crc).toArray(), CRC_FILES[id - 1], BufferType.INT)
        }
        listener?.notify(1.0, "Successfully updated index $id.")
        return true
    }

    private fun readArchiveProperties(fileId: String, type: BufferType): IntArray? {
        if (id == CONFIG_INDEX || id > VERSION_FILES.size) {
            return null
        }
        val data = origin.index(CONFIG_INDEX).archive(VERSION_ARCHIVE)?.file(fileId)?.data ?: return null
        val buffer = InputBuffer(data)
        val properties = IntArray(data.size / (1 shl type.ordinal))
        val bufferFun: () -> Int = when (type) {
            BufferType.BYTE -> {
                { buffer.readUnsignedByte() }
            }
            BufferType.SHORT -> {
                { buffer.readUnsignedShort() }
            }
            BufferType.INT -> {
                { buffer.readInt() }
            }
        }
        for (i in properties.indices) {
            properties[i] = bufferFun()
        }
        return properties
    }

    private fun writeArchiveProperties(properties: IntArray, fileId: String, type: BufferType): Boolean {
        if (id == CONFIG_INDEX || id > VERSION_FILES.size) {
            return false
        }
        val buffer = OutputBuffer(properties.size)
        val bufferFun: (Int) -> Unit = when (type) {
            BufferType.BYTE -> {
                { buffer.writeByte(it) }
            }
            BufferType.SHORT -> {
                { buffer.writeShort(it) }
            }
            BufferType.INT -> {
                { buffer.writeInt(it) }
            }
        }
        properties.forEach(bufferFun)
        val index = origin.index(CONFIG_INDEX)
        index.archive(VERSION_ARCHIVE)?.add(fileId, buffer.array())
        return index.update()
    }

    override fun toHash(name: String): Int {
        return name.hashCode317()
    }

    override fun isIndexValid(index: Int): Boolean {
        return id == index - 1
    }

    override fun indexToWrite(index: Int): Int {
        return index + 1
    }

    companion object {
        private const val CONFIG_INDEX = 0

        private const val VERSION_ARCHIVE = 5

        private val VERSION_FILES = mutableListOf("model_version", "anim_version", "midi_version", "map_version")
        private val CRC_FILES = mutableListOf("model_crc", "anim_crc", "midi_crc", "map_crc")

        @JvmStatic
        fun addMetaFiles(versionFile: String, crcFile: String) {
            VERSION_FILES.add(versionFile)
            CRC_FILES.add(crcFile)
        }
    }

    private enum class BufferType {
        BYTE,
        SHORT,
        INT
    }

}