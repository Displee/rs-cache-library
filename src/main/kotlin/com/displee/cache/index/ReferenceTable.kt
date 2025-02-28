package com.displee.cache.index

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index.Companion.WHIRLPOOL_SIZE
import com.displee.cache.index.archive.Archive
import com.displee.cache.index.archive.Archive317
import com.displee.cache.index.archive.file.File
import com.displee.compress.CompressionType
import com.displee.compress.decompress
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import java.util.*
import kotlin.collections.ArrayList

open class ReferenceTable(protected val origin: CacheLibrary, val id: Int) : Comparable<ReferenceTable> {

    var revision = 0
    private var mask = 0x0
    private var needUpdate = false
    protected var archives: SortedMap<Int, Archive> = TreeMap()
    private var archiveNames = mutableListOf<Int>()

    var version = 0

    override fun compareTo(other: ReferenceTable): Int {
        return id.compareTo(other.id)
    }

    open fun read(buffer: InputBuffer) {
        version = buffer.readUnsignedByte()
        if (version < 5 || version > 7) {
            throw RuntimeException("Unknown version: $version")
        }
        revision = if (version >= 6) buffer.readInt() else 0
        mask = buffer.readByte().toInt()
        val named = mask and FLAG_NAME != 0
        val whirlpool = mask and FLAG_WHIRLPOOL != 0
        val lengths = mask and FLAG_LENGTHS != 0
        val checksums = mask and FLAG_CHECKSUMS != 0

        val readFun: () -> (Int) = if (version >= 7) {
            {
                buffer.readBigSmart()
            }
        } else {
            {
                buffer.readUnsignedShort()
            }
        }

        val archiveIds = IntArray(readFun())
        for (i in archiveIds.indices) {
            val archiveId = readFun() + if (i == 0) 0 else archiveIds[i - 1]
            archiveIds[i] = archiveId.also { archives[i] = Archive(it) }
        }
        val archives = archives()
        archiveNames = ArrayList(archives.size)
        if (named) {
            archives.forEach {
                it.hashName = buffer.readInt()
                if (it.hashName != 0) {
                    archiveNames.add(it.hashName)
                }
            }
        }
        archives.forEach { it.crc = buffer.readInt() }
        if (checksums) {
            archives.forEach { it.checksum = buffer.readInt() }
        }
        if (whirlpool) {
            archives.forEach {
                var archiveWhirlpool = it.whirlpool
                if (archiveWhirlpool == null) {
                    archiveWhirlpool = ByteArray(WHIRLPOOL_SIZE)
                    it.whirlpool = archiveWhirlpool
                }
                buffer.readBytes(archiveWhirlpool)
            }
        }
        if (lengths) {
            archives.forEach {
                it.length = buffer.readInt()
                it.uncompressedLength = buffer.readInt()
            }
        }
        archives.forEach { it.revision = buffer.readInt() }
        val archiveFileSizes = IntArray(archives.size)
        for (i in archives.indices) {
            archiveFileSizes[i] = readFun()
        }
        for (i in archives.indices) {
            val archive = archives[i]
            var fileId = 0
            for (fileIndex in 0 until archiveFileSizes[i]) {
                fileId += readFun()
                archive.files[fileId] = File(fileId)
            }
        }
        if (named) {
            for (i in archives.indices) {
                val archive = archives[i]
                val fileIds = archive.fileIds()
                for (fileIndex in 0 until archiveFileSizes[i]) {
                    archive.file(fileIds[fileIndex])?.hashName = buffer.readInt()
                }
            }
        }

        this.archives.clear()
        archives.forEach { this.archives.putIfAbsent(it.id, it) }
    }

    open fun write(): ByteArray {
        val buffer = OutputBuffer(10_000_000) //10mb
        buffer.writeByte(version)
        if (version >= 6) {
            buffer.writeInt(revision)
        }
        buffer.writeByte(mask)

        val writeFun: (Int) -> Unit = if (version >= 7) {
            {
                buffer.writeBigSmart(it)
            }
        } else {
            {
                buffer.writeShort(it)
            }
        }

        writeFun(archives.size)
        val archiveIds = archiveIds()
        val archives = archives()
        for (i in archives.indices) {
            writeFun(archiveIds[i] - if (i == 0) 0 else archiveIds[i - 1])
        }
        if (isNamed()) {
            archives.forEach { buffer.writeInt(it.hashName) }
        }
        archives.forEach { buffer.writeInt(it.crc) }
        if (hasChecksums()) {
            archives.forEach { buffer.writeInt(it.checksum) }
        }
        if (hasWhirlpool()) {
            val empty = ByteArray(WHIRLPOOL_SIZE)
            archives.forEach { buffer.writeBytes(it.whirlpool ?: empty) }
        }
        if (hasLengths()) {
            archives.forEach { buffer.writeInt(it.length).writeInt(it.uncompressedLength) }
        }
        archives.forEach { buffer.writeInt(it.revision) }
        archives.forEach { writeFun(it.files.size) }
        archives.forEach {
            val fileIds = it.fileIds()
            for (fileIndex in fileIds.indices) {
                writeFun(fileIds[fileIndex] - if (fileIndex == 0) 0 else fileIds[fileIndex - 1])
            }
        }
        if (isNamed()) {
            archives.forEach { archive ->
                archive.files().forEach { file ->
                    buffer.writeInt(file.hashName)
                }
            }
        }
        return buffer.array()
    }

    @JvmOverloads
    fun add(data: ByteArray, xtea: IntArray? = null): Archive {
        val archive = add(xtea = xtea)
        archive.add(data)
        return archive
    }

    fun add(vararg archives: Archive?, overwrite: Boolean = true): Array<Archive> {
        val newArchives = ArrayList<Archive>(archives.size)
        archives.forEach {
            it ?: return@forEach
            newArchives.add(add(it, overwrite = overwrite))
        }
        return newArchives.toTypedArray()
    }

    @JvmOverloads
    fun add(archive: Archive, newId: Int = archive.id, xtea: IntArray? = null, overwrite: Boolean = true): Archive {
        val new = add(newId, archive.hashName, xtea, overwrite)
        if (overwrite) {
            new.clear()
            new.add(*archive.copyFiles())
            new.flag()
        }
        return new
    }

    @JvmOverloads
    fun add(name: String? = null, xtea: IntArray? = null): Archive {
        var id = if (name == null) nextId() else archiveId(name)
        if (id == -1) {
            id = nextId()
        }
        return add(id, toHash(name ?: ""), xtea)
    }

    @JvmOverloads
    fun add(id: Int, hashName: Int = -1, xtea: IntArray? = null, overwrite: Boolean = true): Archive {
        var existing = archive(id, direct = true)
        if (existing != null && !existing.read && !existing.new && !existing.flagged()) {
            existing = archive(id, xtea)
        }
        if (existing == null) {
            if (this is Index317) {
                existing = Archive317(origin.compressors.bzip2, id, if (hashName == -1) 0 else hashName)
                if (this.id != 0) {
                    existing.compressionType = CompressionType.GZIP
                    existing.compressor = origin.compressors.gzip
                }
            } else {
                existing = Archive(id, if (hashName == -1) 0 else hashName, xtea)
                existing.compressionType = CompressionType.GZIP
                existing.compressor = origin.compressors.gzip
            }
            if (hashName != -1) {
                archiveNames.add(existing.hashName)
            }
            archives[id] = existing
            existing.new = true
            existing.flag()
            flag()
        } else if (overwrite) {
            var flag = false
            val existingXtea = existing.xtea
            if (xtea == null && existingXtea != null || xtea != null && existingXtea == null ||
                    xtea != null && existingXtea != null && !xtea.contentEquals(existingXtea)) {
                existing.xtea = xtea
                flag = true
            }
            if (hashName != -1 && existing.hashName != hashName) {
                archiveNames.remove(existing.hashName)
                existing.hashName = hashName
                archiveNames.add(existing.hashName)
                flag = true
            }
            if (flag) {
                existing.flag()
                flag()
            }
        }
        return existing
    }

    fun archive(name: String, direct: Boolean = false): Archive? {
        return archive(archiveId(name), direct)
    }

    @JvmOverloads
    fun archive(name: String, xtea: IntArray? = null, direct: Boolean = false): Archive? {
        return archive(archiveId(name), xtea, direct)
    }

    fun archive(id: Int, direct: Boolean = false): Archive? {
        return archive(id, null, direct)
    }

    @JvmOverloads
    open fun archive(id: Int, xtea: IntArray? = null, direct: Boolean = false): Archive? {
        check(!origin.closed) { "Cache is closed." }
        val archive = archives[id] ?: return null
        if (direct || archive.read || archive.new) {
            return archive
        }
        val sector = origin.index(this.id).readArchiveSector(id)
        if (sector == null) {
            archive.read = true
            archive.new = true
            archive.clear()
        } else {
            val is317 = is317()
            if (is317) {
                archive.compressionType = if (this.id == 0) CompressionType.BZIP2 else CompressionType.GZIP
                archive.compressor = origin.compressors.get(archive.compressionType)
                archive.read(InputBuffer(sector.data))
            } else {
                val decompressed = sector.decompress(origin.compressors, xtea)
                archive.compressionType = sector.compressionType
                archive.compressor = sector.compressor
                if (decompressed.isNotEmpty()) {
                    archive.read(InputBuffer(decompressed))
                    archive.xtea = xtea
                }
            }
            val mapsIndex = if (is317) 4 else 5
            if (this.id == mapsIndex && !archive.containsData()) {
                archive.read = false
            }
            if (!is317) {
                val sectorBuffer = InputBuffer(sector.data)
                sectorBuffer.offset = 1
                val remaining: Int = sector.data.size - (sectorBuffer.readInt() + sectorBuffer.offset)
                if (remaining >= 2) {
                    sectorBuffer.offset = sector.data.size - 2
                    archive.revision = sectorBuffer.readUnsignedShort()
                }
            }
        }
        return archive
    }

    fun contains(id: Int): Boolean {
        return archives.containsKey(id)
    }

    fun contains(name: String): Boolean {
        return archiveNames.contains(toHash(name))
    }

    fun remove(id: Int): Archive? {
        val archive = archives.remove(id) ?: return null
        archiveNames.remove(archive.hashName)
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
        val hashName = toHash(name)
        archives.values.forEach {
            if (it.hashName == hashName) {
                return it.id
            }
        }
        return -1
    }

    fun nextId(): Int {
        val last = last()
        return if (last == null) 0 else last.id + 1
    }

    fun copyArchives(): Array<Archive> {
        val archives = archives()
        val copy = ArrayList<Archive>(archives.size)
        for (i in archives.indices) {
            copy.add(i, Archive(archives[i]))
        }
        return copy.toTypedArray()
    }

    fun flag() {
        needUpdate = true
    }

    fun flagged(): Boolean {
        return needUpdate
    }

    fun unFlag() {
        needUpdate = false
    }

    fun mask(referenceTable: ReferenceTable) {
        this.mask = referenceTable.mask
    }

    fun flagMask(flag: Int) {
        mask = mask or flag
    }

    fun unFlagMask(flag: Int) {
        mask = mask and flag.inv()
    }

    fun isNamed(): Boolean {
        return mask and FLAG_NAME != 0
    }

    fun hasWhirlpool(): Boolean {
        return mask and FLAG_WHIRLPOOL != 0
    }

    fun hasLengths(): Boolean {
        return mask and FLAG_LENGTHS != 0
    }

    fun hasChecksums(): Boolean {
        return mask and FLAG_CHECKSUMS != 0
    }

    fun archiveIds(): IntArray {
        return archives.keys.toIntArray()
    }

    fun archives(): Array<Archive> {
        return archives.values.toTypedArray()
    }

    open fun toHash(name: String): Int {
        return name.hashCode()
    }

    fun is317(): Boolean {
        return this is Index317
    }

    companion object {
        const val FLAG_NAME = 0x1
        const val FLAG_WHIRLPOOL = 0x2
        const val FLAG_LENGTHS = 0x4
        const val FLAG_CHECKSUMS = 0x8
    }

}