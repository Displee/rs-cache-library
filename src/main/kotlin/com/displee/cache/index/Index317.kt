package com.displee.cache.index

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.displee.cache.index.archive.Archive
import com.displee.cache.index.archive.Archive317
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.CRCHash
import com.displee.util.Utils
import com.displee.util.Whirlpool
import com.displee.util.hashCode317
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class Index317(origin: CacheLibrary, id: Int, randomAccessFile: RandomAccessFile) : Index(origin, id, randomAccessFile) {

    override fun update(xteas: Map<Int, IntArray>, listener: ProgressListener?): Boolean {
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
            val compressed = it.write()
            it.crc = CRCHash.generate(compressed)
            it.whirlpool = Whirlpool.generate(compressed)
            val written = writeArchiveSector(it.id, compressed)
            check(written) { "Unable to write data to archive sector. Your cache may be corrupt." }
            if (origin.clearDataAfterUpdate) {
                it.restore()
            }
        }
        listener?.notify(85.0, "Updating version archive for index $id...")
        if (updateCount != 0 && !flagged()) {
            flag()
        }
        if (id != CONFIG_INDEX && id < VERSION_NAMES.size && flagged()) {
            writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::revision).toArray(), VERSION_NAMES[id - 1], BufferType.SHORT)
            writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::crc).toArray(), CRC_NAMES[id - 1], BufferType.INT)
            writeArchiveProperties(Arrays.stream(archives).mapToInt { e: Archive -> (e as Archive317).priority }.toArray(), INDEX_NAMES[id - 1], nameBufferType())
        }
        listener?.notify(100.0, "Successfully updated index $id.")
        return true
    }

    override fun init() {
        val archiveLength = try {
            (randomAccessFile.length() / INDEX_SIZE.toLong()).toInt()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        var versions: IntArray? = null
        var crcs: IntArray? = null
        var priorities: IntArray? = null
        if (id != CONFIG_INDEX && id < VERSION_NAMES.size) {
            versions = readArchiveProperties(VERSION_NAMES[id - 1], BufferType.SHORT)
            crcs = readArchiveProperties(CRC_NAMES[id - 1], BufferType.INT)
            priorities = readArchiveProperties(INDEX_NAMES[id - 1], nameBufferType())
        }
        for (i in 0 until archiveLength) {
            val archive = Archive317(i)
            archives[i] = archive
            if (versions == null || crcs == null || i >= versions.size) {
                continue
            }
            archive.revision = versions[i]
            archive.crc = crcs[i]
            if (priorities != null) {
                archive.priority = if (i < priorities.size) priorities[i] else 0
            }
        }
    }

    override fun read(buffer: InputBuffer) {

    }

    override fun write(): ByteArray {
        return ByteArray(0)
    }

    private fun readArchiveProperties(fileId: String, type: BufferType): IntArray? {
        if (id == CONFIG_INDEX || id == MAPS_INDEX || id > VERSION_NAMES.size) {
            return null
        }
        val data = origin.index(CONFIG_INDEX)?.archive(VERSION_ARCHIVE)?.file(fileId)?.data
        if (data == null) {
            System.err.println("Missing file data for $fileId, type=$type.")
            return null
        }
        val buffer = InputBuffer(data)
        val properties = IntArray(data.size / (1 shl type.ordinal))
        val bufferFun: () -> Int = when (type) {
            BufferType.BYTE -> {
                { buffer.readUnsigned() }
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
        if (id == CONFIG_INDEX || id == MAPS_INDEX || id > VERSION_NAMES.size) {
            return false
        }
        val buffer = OutputBuffer(properties.size)
        val bufferFun: (Int) -> Unit = when (type) {
            BufferType.BYTE -> {
                { buffer.write(it) }
            }
            BufferType.SHORT -> {
                { buffer.writeShort(it) }
            }
            BufferType.INT -> {
                { buffer.writeInt(it) }
            }
        }
        properties.forEach(bufferFun)
        val index = origin.index(CONFIG_INDEX) ?: return false
        index.archive(VERSION_ARCHIVE)?.add(fileId, buffer.array())
        return index.update()
    }

    private fun nameBufferType(): BufferType {
        return if (id == ANIMATION_INDEX) BufferType.SHORT else BufferType.BYTE
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
        private const val ANIMATION_INDEX = 2
        private const val MAPS_INDEX = 4

        private const val VERSION_ARCHIVE = 5

        private val VERSION_NAMES = arrayOf("model_version", "anim_version", "midi_version", "map_version")
        private val CRC_NAMES = arrayOf("model_crc", "anim_crc", "midi_crc", "map_crc")
        private val INDEX_NAMES = arrayOf("model_index", "anim_index", "midi_index", "map_index")
    }

    private enum class BufferType {
        BYTE,
        SHORT,
        INT
    }

}