package org.displee.cache.index.archive

import org.displee.cache.index.archive.file.File
import org.displee.io.impl.InputStream
import org.displee.io.impl.OutputStream
import org.displee.utilities.Miscellaneous
import java.util.*

open class Archive(val id: Int, var hashName: Int = 0) {

    var revision = 0
    private var needUpdate = false
    val files: SortedMap<Int, File> = TreeMap()

    var crc: Int = 0
    var whirlpool: ByteArray? = null
    var flag8Value = 0
    var flag4Value1 = 0
    var flag4Value2 = 0

    var read = false
    var new = false

    constructor(id: Int) : this(id, 0)

    constructor(archive: Archive): this(archive.id, archive.hashName) {
        for (file in archive.files()) {
            files[file.id] = File(file)
        }
        revision = archive.revision
        crc = archive.crc
        whirlpool = archive.whirlpool?.clone()
    }

    open fun read(inputStream: InputStream): Boolean {
        read = true
        if (files.size == 1) {
            first()?.data = inputStream.bytes
            return true
        }
        val fileIds = fileIds()
        var fileDataSizesOffset = inputStream.bytes.size
        val length: Int = inputStream.bytes[--fileDataSizesOffset].toInt() and 0xFF
        fileDataSizesOffset -= length * (fileIds.size * 4)
        val fileDataSizes = IntArray(fileIds.size)
        inputStream.offset = fileDataSizesOffset
        for (i in 0 until length) {
            var offset = 0
            for (fileIndex in fileIds.indices) {
                offset += inputStream.readInt()
                fileDataSizes[fileIndex] += offset
            }
        }
        val filesData = arrayOfNulls<ByteArray>(fileIds.size)
        for (i in fileIds.indices) {
            filesData[i] = ByteArray(fileDataSizes[i])
            fileDataSizes[i] = 0
        }
        inputStream.offset = fileDataSizesOffset
        var offset = 0
        for (i in 0 until length) {
            var read = 0
            for (j in fileIds.indices) {
                read += inputStream.readInt()
                System.arraycopy(inputStream.bytes, offset, filesData[j], fileDataSizes[j], read)
                offset += read
                fileDataSizes[j] += read
            }
        }
        for (i in fileIds.indices) {
            file(fileIds[i])?.data = filesData[i]
        }
        return true
    }

    open fun write(): ByteArray {
        val files = files()
        var size = 0
        for(file in files) {
            size += file.data?.size ?: 0
        }
        val buffer = OutputStream(size)
        if (files.size == 1) {
            return first()?.data ?: byteArrayOf()
        } else {
            for (file in files) {
                buffer.writeBytes(file.data ?: byteArrayOf())
            }
            for (i in files.indices) {
                val file = files[i]
                val dataSize = file.data?.size ?: 0
                val previousFileDataSize = if (i == 0) 0 else files[i - 1].data?.size ?: 0
                buffer.writeInt(dataSize - previousFileDataSize)
            }
        }
        buffer.writeByte(1)
        return buffer.flip()
    }

    fun containsData(): Boolean {
        for (entry in files.values) {
            if (entry.data != null) {
                return true
            }
        }
        return false
    }

    fun add(vararg files: File): Array<File> {
        val newFiles = ArrayList<File>(files.size)
        files.forEach { newFiles.add(add(it)) }
        return newFiles.toTypedArray()
    }

    fun add(file: File): File {
        val fileData = file.data
        checkNotNull(fileData) { "File data is null." }
        return add(file.id, fileData, file.hashName)
    }

    //TODO Put hash name in front
    fun add(data: ByteArray, hashName: Int): File {
        return add(nextId(), data, hashName)
    }

    fun add(id: Int, data: ByteArray): File {
        return add(id, data, 0)
    }

    fun add(id: Int, data: ByteArray, hashName: Int): File {
        var file = files[id]
        if (file == null) {
            file = File(id, data, hashName)
            files[id] = file
            flag()
        } else {
            var flag = false
            if (!Arrays.equals(file.data, data)) {
                file.data = data
                flag = true
            }
            if (file.hashName != hashName) {
                file.hashName = hashName
                flag = true
            }
            if (flag) {
                flag()
            }
        }
        return file
    }

    fun file(id: Int): File? {
        return files[id]
    }

    fun file(data: ByteArray): File? {
        return files.filterValues { Arrays.equals(it.data, data) }.values.firstOrNull()
    }

    fun file(name: String): File? {
        return if (this is Archive317) {
            files.filterValues { it.hashName == Miscellaneous.to317Hash(name) }.values.firstOrNull()
        } else {
            files.filterValues { it.hashName == name.hashCode() }.values.firstOrNull()
        }
    }

    fun remove(id: Int): File? {
        val file = files.remove(id)
        flag()
        return file
    }

    fun first(): File? {
        if (files.isEmpty()) {
            return null
        }
        return file(files.firstKey())
    }

    fun last(): File? {
        if (files.isEmpty()) {
            return null
        }
        return files[files.lastKey()]
    }

    fun nextId(): Int {
        val last = last()
        return if (last == null) 0 else last.id + 1
    }

    fun copyFiles(): Array<File> {
        val files = files()
        val copy = ArrayList<File>(files.size)
        for(i in files.indices) {
            copy[i] = File(files[i])
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
        revision--
        needUpdate = false
    }

    fun restore() {
        for (file in files.values) {
            file.data = null
        }
        read = false
        new = false
    }

    fun clear() {
        files.clear()
    }

    fun fileIds(): IntArray {
        return files.keys.toIntArray()
    }

    fun files(): Array<File> {
        return files.values.toTypedArray()
    }

    override fun toString(): String {
        return "Archive[id=$id, hash_name=$hashName, revision=$revision, crc=$crc, has_whirlpool=${whirlpool != null}, read=$read, files_size=${files.size}]"
    }

}