package com.displee.cache.index.archive

import com.displee.cache.index.Index.Companion.SECTOR_HEADER_SIZE_BIG
import com.displee.cache.index.Index.Companion.SECTOR_HEADER_SIZE_SMALL
import com.displee.compress.CompressionType
import com.displee.compress.type.Compressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer

class ArchiveSector(private val bigSector: Boolean, val size: Int, var position: Int, var id: Int = 0, var index: Int = 0) {

    var chunk = 0
    var nextPosition = 0
    var data = ByteArray(size)

    lateinit var compressionType: CompressionType
    lateinit var compressor: Compressor

    fun read(buffer: InputBuffer) {
        id = if (bigSector) {
            buffer.readInt()
        } else {
            buffer.readUnsignedShort()
        }
        chunk = buffer.readUnsignedShort()
        nextPosition = buffer.read24BitInt()
        index = buffer.readUnsignedByte()
    }

    fun write(): ByteArray {
        val buffer = OutputBuffer(if (bigSector) SECTOR_HEADER_SIZE_BIG else SECTOR_HEADER_SIZE_SMALL)
        if (bigSector) {
            buffer.writeInt(id)
        } else {
            buffer.writeShort(id)
        }
        return buffer.writeShort(chunk).write24BitInt(position).writeByte(index).array()
    }

}