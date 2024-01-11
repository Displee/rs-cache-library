package com.displee.cache.index.archive

import com.displee.cache.index.archive.file.File
import com.displee.compress.CompressionType
import com.displee.compress.type.BZIP2Compressor
import com.displee.compress.type.EmptyCompressor
import com.displee.compress.type.GZIPCompressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.hashCode317

class Archive317(private val bzip2: BZIP2Compressor, id: Int, name: Int) : Archive(id, name) {

    private var extracted = false

    constructor(bzip2: BZIP2Compressor, id: Int) : this(bzip2, id, 0)

    override fun read(buffer: InputBuffer) {
        read = true
        if (compressionType == CompressionType.GZIP) {
            files[0] = File(0, (compressor as GZIPCompressor).deflate317(buffer.raw()))
            return
        }
        var decompressedLength = buffer.read24BitInt()
        var compressedLength = buffer.read24BitInt()
        if (decompressedLength != compressedLength) {
            extracted = true
        }
        var compressor = if (extracted) bzip2 else EmptyCompressor
        val decompressed = compressor.decompress(buffer, compressedLength, decompressedLength)
        val metaBuffer = InputBuffer(decompressed)
        val filesLength = metaBuffer.readUnsignedShort()
        val filesBuffer = InputBuffer(decompressed)
        filesBuffer.offset = metaBuffer.offset + filesLength * 10
        compressor = if (extracted) EmptyCompressor else bzip2
        for (i in 0 until filesLength) {
            val fileName = metaBuffer.readInt()
            decompressedLength = metaBuffer.read24BitInt()
            compressedLength = metaBuffer.read24BitInt()
            val data: ByteArray = compressor.decompress(filesBuffer, compressedLength, decompressedLength)
            files[i] = File(i, data, fileName)
        }
    }

    override fun write(): ByteArray {
        if (compressionType == CompressionType.GZIP) {
            return compressor.compress(first()?.data ?: byteArrayOf())
        }
        val metaBuffer = OutputBuffer(2 + files.size * 10)
        metaBuffer.writeShort(files.size)
        val filesBuffer = OutputBuffer(2048)
        var compressor = if (extracted) EmptyCompressor else bzip2
        for (file in files.values) {
            val fileData = file.data ?: continue
            metaBuffer.writeInt(file.hashName).write24BitInt(fileData.size)
            val toWrite = compressor.compress(fileData)
            metaBuffer.write24BitInt(toWrite.size)
            filesBuffer.writeBytes(toWrite)
        }
        metaBuffer.writeBytes(filesBuffer.array())
        val decompressed = metaBuffer.array()
        compressor = if (extracted) bzip2 else EmptyCompressor
        val compressed = compressor.compress(decompressed)
        val buffer = OutputBuffer(compressed.size + 6)
        buffer.write24BitInt(decompressed.size).write24BitInt(compressed.size).writeBytes(compressed)
        return buffer.array()
    }

    override fun toHash(name: String): Int {
        return name.hashCode317()
    }

}