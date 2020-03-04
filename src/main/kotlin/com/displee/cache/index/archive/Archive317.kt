package com.displee.cache.index.archive

import com.displee.cache.index.archive.file.File
import com.displee.compress.CompressionType
import com.displee.cache.compress.type.BZIP2Compressor
import com.displee.compress.type.GZIPCompressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.hashCode317

class Archive317(id: Int, name: Int) : Archive(id, name) {

    private var extracted = false

    constructor(id: Int) : this(id, 0)

    override fun read(buffer: InputBuffer) {
        read = true
        if (compressionType == CompressionType.GZIP) {
            files[0] = File(0, GZIPCompressor.deflate317(buffer.raw()))
            return
        }
        val decompressed: ByteArray
        var decompressedLength = buffer.read24BitInt()
        var compressedLength = buffer.read24BitInt()
        if (decompressedLength != compressedLength) {
            decompressed = BZIP2Compressor.decompress317(decompressedLength, compressedLength, buffer)
            extracted = true
        } else {
            decompressed = buffer.readBytes(buffer.remaining())
        }
        val metaBuffer = InputBuffer(decompressed)
        val filesLength = metaBuffer.readUnsignedShort()
        val filesBuffer = InputBuffer(decompressed)
        filesBuffer.offset = metaBuffer.offset + filesLength * 10
        for (i in 0 until filesLength) {
            val fileName = metaBuffer.readInt()
            decompressedLength = metaBuffer.read24BitInt()
            compressedLength = metaBuffer.read24BitInt()
            val data: ByteArray = if (extracted) {
                filesBuffer.readBytes(decompressedLength)
            } else {
                BZIP2Compressor.decompress317(decompressedLength, compressedLength, filesBuffer)
            }
            files[i] = File(i, data, fileName)
        }
    }

    override fun write(): ByteArray {
        if (compressionType == CompressionType.GZIP) {
            return GZIPCompressor.inflate317(first()?.data ?: byteArrayOf())
        }
        val metaBuffer = OutputBuffer(2 + files.size * 10)
        metaBuffer.writeShort(files.size)
        val filesBuffer = OutputBuffer(2048)
        for (file in files.values) {
            val fileData = file.data ?: continue
            metaBuffer.writeInt(file.hashName).write24BitInt(fileData.size)
            val toWrite = if (extracted) fileData else BZIP2Compressor.compress(file.data)
            metaBuffer.write24BitInt(toWrite.size)
            filesBuffer.writeBytes(toWrite)
        }
        metaBuffer.writeBytes(filesBuffer.array())
        val decompressed = metaBuffer.array()
        val compressed = if (extracted) BZIP2Compressor.compress(decompressed) else decompressed
        val buffer = OutputBuffer(compressed.size + 6)
        buffer.write24BitInt(decompressed.size).write24BitInt(compressed.size).writeBytes(compressed)
        return buffer.array()
    }

    override fun toHash(name: String): Int {
        return name.hashCode317()
    }

}