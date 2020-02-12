package com.displee.cache.index.archive

import com.displee.cache.index.Index.Companion.SECTOR_HEADER_SIZE_BIG
import com.displee.cache.index.Index.Companion.SECTOR_HEADER_SIZE_SMALL
import com.displee.compress.CompressionType
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer

class ArchiveSector(private val bigHeader: Boolean, val size: Int, var position: Int, var id: Int = 0, var index: Int = 0) {

    var chunk = 0
    var nextPosition = 0
    var data = ByteArray(size)

    lateinit var compressionType: CompressionType

    fun read(buffer: InputBuffer) {
        id = if (bigHeader) {
            buffer.readInt()
        } else {
            buffer.readUnsignedShort()
        }
        chunk = buffer.readUnsignedShort()
        nextPosition = buffer.read24BitInt()
        index = buffer.readUnsigned()
    }

    fun write(): ByteArray {
        val buffer = OutputBuffer(if (bigHeader) SECTOR_HEADER_SIZE_BIG else SECTOR_HEADER_SIZE_SMALL)
        if (bigHeader) {
            buffer.writeInt(id)
        } else {
            buffer.writeShort(id)
        }
        buffer.writeShort(chunk)
        buffer.write24BitInt(position)
        buffer.write(index)
        return buffer.array()
    }

}