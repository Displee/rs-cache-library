package org.displee.cache.index

import org.displee.CacheLibrary
import org.displee.cache.index.archive.Archive
import org.displee.cache.index.archive.file.File
import org.displee.io.impl.InputStream
import org.displee.io.impl.OutputStream
import org.displee.utilities.Compression
import org.displee.utilities.Constants
import org.displee.utilities.Miscellaneous
import java.util.*
import kotlin.collections.ArrayList

open class ReferenceTable(protected val origin: CacheLibrary, protected val id: Int) {

    protected var revision = 0
    var mask = 0x0
    private var needUpdate = false
    protected var archives: SortedMap<Int, Archive> = TreeMap()

    var version = 0

    fun read(inputStream: InputStream) {
        version = inputStream.readUnsignedByte()
        if (version < 5 || version > 7) {
            throw RuntimeException("Unknown version: $version")
        }
        revision = if (version >= 6) inputStream.readInt() else 0
        mask = inputStream.readByte()
        val named = mask and FLAG_NAME != 0
        val whirlpool = mask and FLAG_WHIRLPOOL != 0
        val flag4 = mask and FLAG_4 != 0
        val flag8 = mask and FLAG_8 != 0

        val readFun: () -> (Int) = if (version >= 7) {
            {
                inputStream.readBigSmart()
            }
        } else {
            {
                inputStream.readUnsignedShort()
            }
        }

        val archiveIds = IntArray(readFun())
        val archives = ArrayList<Archive>(archiveIds.size)
        for (i in archiveIds.indices) {
            archiveIds[i] = readFun() + if (i == 0) 0 else archiveIds[i - 1]
            archives[i] = Archive(archiveIds[i])
        }
        if (named) {
            archives.forEach { it.hashName = inputStream.readInt() }
        }
        if (origin.isRS3) {
            archives.forEach { it.crc = inputStream.readInt() }
            if (flag8) {
                archives.forEach { it.flag8Value = inputStream.readInt() }
            }
            if (whirlpool) {
                archives.forEach { inputStream.readBytes(it.whirlpool, 0, Constants.WHIRLPOOL_SIZE) }
            }
            if (flag4) {
                archives.forEach {
                    it.flag4Value1 = inputStream.readInt()
                    it.flag4Value2 = inputStream.readInt()
                }
            }
        } else {
            if (whirlpool) {
                archives.forEach { inputStream.readBytes(it.whirlpool, 0, Constants.WHIRLPOOL_SIZE) }
            }
            archives.forEach { it.crc = inputStream.readInt() }
        }
        archives.forEach { it.revision = inputStream.readInt() }
        val archiveFileSizes = IntArray(archives.size)
        for (i in archives.indices) {
            archiveFileSizes[i] = readFun()
        }
        for (i in archives.indices) {
            val archive = archives[i]
            for (fileIndex in 0 until archiveFileSizes[i]) {
                val fileId = readFun() + fileIndex
                archive.files[fileId] = File(fileId)
            }
        }
        if (named) {
            for (i in archives.indices) {
                val archive = archives[i]
                val fileIds = archive.fileIds()
                for (fileIndex in 0 until archiveFileSizes[i]) {
                    archive.file(fileIds[fileIndex])?.hashName = inputStream.readInt()
                }
            }
        }
    }

    fun write(): ByteArray {
        val outputStream = OutputStream(1000)
        outputStream.writeByte(version)
        if (version >= 6) {
            outputStream.writeInt(revision)
        }
        outputStream.writeByte(mask)

        val writeFun: (Int) -> Unit = if (version >= 7) {
            {
                outputStream.writeBigSmart(it)
            }
        } else {
            {
                outputStream.writeShort(it)
            }
        }

        writeFun(archives.size)
        val archiveIds = archiveIds()
        val archives = archives()
        for (i in archives.indices) {
            writeFun(archiveIds[i] - if (i == 0) 0 else archiveIds[i - 1])
        }
        if (isNamed()) {
            archives.forEach { outputStream.writeInt(it.hashName) }
        }
        if (origin.isRS3) {
            archives.forEach { outputStream.writeInt(it.crc) }
            if (hasFlag8()) {
                archives.forEach { outputStream.writeInt(it.flag8Value) }
            }
            if (hasWhirlpool()) {
                archives.forEach { outputStream.writeBytes(it.whirlpool) }
            }
            if (hasFlag4()) {
                archives.forEach {
                    outputStream.writeInt(it.flag4Value1)
                    outputStream.writeInt(it.flag4Value2)
                }
            }
        } else {
            if (hasWhirlpool()) {
                archives.forEach { outputStream.writeBytes(it.whirlpool) }
            }
            archives.forEach { outputStream.writeInt(it.crc) }
        }
        archives.forEach { outputStream.writeInt(it.revision) }
        archives.forEach {
            writeFun(it.files.size)
        }
        archives.forEach {
            val fileIds = it.fileIds()
            for (fileIndex in fileIds.indices) {
                writeFun(fileIds[fileIndex] - if (fileIndex == 0) 0 else fileIds[fileIndex - 1])
            }
        }
        if (isNamed()) {
            archives.forEach { archive ->
                archive.files().forEach { file ->
                    outputStream.writeInt(file.hashName)
                }
            }
        }
        return outputStream.flip()
    }

    fun add(vararg archives: Archive): Array<Archive> {
        val newArchives = ArrayList<Archive>(archives.size)
        newArchives.forEach { newArchives.add(add(it)) }
        return newArchives.toTypedArray()
    }

    fun add(archive: Archive): Archive {
        return add(archive.id, archive.hashName)
    }

    fun add(id: Int, hashName: Int = 0): Archive {
        var existing = archive(id, direct = true)
        if (existing != null && !existing.read && !existing.new && !existing.flagged()) {
            existing = archive(id)
        }
        if (existing == null) {
            existing = Archive(id, hashName)
            existing.new = true
            existing.flag()
            flag()
        } else {
            var flag = false
            if (existing.hashName != hashName) {
                existing.hashName = hashName
                flag = true
            }
            if (flag) {
                existing.flag()
                flag()
            }
        }
        return existing
    }

    fun archive(name: String, xtea: IntArray? = null, direct: Boolean = false): Archive? {
        return archive(archiveId(name), xtea, direct)
    }

    fun archive(id: Int, xtea: IntArray? = null, direct: Boolean = false): Archive? {
        check(!origin.isClosed)
        val archive = archives[id] ?: return null
        if (direct || archive.read || archive.new) {
            return archive
        }
        val sector = origin.getIndex(this.id).readArchiveSector(id)
        if (sector == null) {
            archive.read = true
            archive.new = true
            archive.clear()
            return archive
        }
        archive.read(InputStream(Compression.decompress(sector, xtea)))
        if (this.id == 5 && !archive.containsData()) {
            archive.read = false
        }
        val inputStream = InputStream(sector.data)
        inputStream.offset = 1
        val remaining: Int = inputStream.bytes.size - (inputStream.readInt() + inputStream.offset)
        if (remaining >= 2) {
            inputStream.offset = inputStream.bytes.size - 2
            archive.revision = inputStream.readUnsignedShort()
        }
        return archive
    }

    fun remove(id: Int): Archive? {
        val archive = archives.remove(id)
        flag()
        return archive
    }

    fun remove(name: String): Archive? {
        return remove(archiveId(name))
    }

    fun first(): Archive? {
        if (archives.isEmpty()) {
            return null
        }
        return archive(archives.firstKey())
    }

    fun last(): Archive? {
        if (archives.isEmpty()) {
            return null
        }
        return archive(archives.lastKey())
    }

    fun archiveId(name: String): Int {
        val unit: (String) -> Int = if (origin.is317) {
            {
                Miscellaneous.to317Hash(it)
            }
        } else {
            {
                it.hashCode()
            }
        }
        archives.values.forEach {
            if (it.hashName == unit(name)) {
                return it.id
            }
        }
        return 0
    }

    fun nextId(): Int {
        val last = last()
        return if (last == null) 0 else last.id + 1
    }

    fun copyArchives(): Array<Archive> {
        val archives = archives()
        val copy = ArrayList<Archive>(archives.size)
        for (i in archives.indices) {
            copy[i] = Archive(archives[i])
        }
        return copy.toTypedArray()
    }

    fun flag() {
        revision++
        needUpdate = true
    }

    fun flagged(): Boolean {
        return needUpdate
    }

    fun unFlag() {
        if (!flagged()) {
            return
        }
        needUpdate = false
    }

    fun flagMask(flag: Int) {
        mask = mask or flag
    }

    fun isNamed(): Boolean {
        return mask and FLAG_NAME != 0
    }

    fun hasWhirlpool(): Boolean {
        return mask and FLAG_WHIRLPOOL != 0
    }

    fun hasFlag4(): Boolean {
        return mask and FLAG_4 != 0
    }

    fun hasFlag8(): Boolean {
        return mask and FLAG_8 != 0
    }

    fun archiveIds(): IntArray {
        return archives.keys.toIntArray()
    }

    fun archives(): Array<Archive> {
        return archives.values.toTypedArray()
    }

    companion object {
        const val FLAG_NAME = 0x1
        const val FLAG_WHIRLPOOL = 0x2
        const val FLAG_4 = 0x4
        const val FLAG_8 = 0x8
    }

}