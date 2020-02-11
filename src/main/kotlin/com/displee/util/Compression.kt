package com.displee.util

import com.displee.cache.index.archive.ArchiveSector
import com.displee.compress.BZIP2Compressor
import com.displee.compress.CompressionType
import com.displee.compress.GZIPCompressor
import com.displee.compress.LZMACompressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer

object Compression {

    fun compress(uncompressed: ByteArray, compressionType: CompressionType, xteas: IntArray? = null, revision: Int = -1): ByteArray {
        val compressed: ByteArray = when (compressionType) {
            CompressionType.NONE -> uncompressed
            CompressionType.BZIP2 -> BZIP2Compressor.compress(uncompressed)
            CompressionType.GZIP -> GZIPCompressor.deflate(uncompressed)
            CompressionType.LZMA -> LZMACompressor.compress(uncompressed)
        }
        val buffer = OutputBuffer(5 + compressed.size + if (revision == -1) 0 else 2)
        buffer.write(compressionType.ordinal).writeInt(compressed.size)
        if (compressionType != CompressionType.NONE) {
            buffer.writeInt(uncompressed.size)
        }
        buffer.write(compressed)
        if (xteas != null && (xteas[0] != 0 || xteas[1] != 0 || xteas[2] != 0 || 0 != xteas[3])) {
            buffer.encodeXTEA(xteas, 5, buffer.offset)
        }
        if (revision != -1) {
            buffer.writeShort(revision)
        }
        return buffer.array()
    }

    fun decompress(archiveSector: ArchiveSector, keys: IntArray? = null): ByteArray {
        val packedData = archiveSector.data
        val inputStream = InputBuffer(packedData)
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            inputStream.decodeXTEA(keys, 5, packedData.size)
        }
        val type = inputStream.readUnsigned()
        val compressionType = CompressionType.compressionTypes[type].also { archiveSector.compressionType = it }
        val compressedSize = inputStream.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (compressionType != CompressionType.NONE) {
            decompressedSize = inputStream.readInt() and 0xFFFFFF
        }
        var decompressed = ByteArray(decompressedSize)
        when (compressionType) {
            CompressionType.NONE -> decompressed = inputStream.read(compressedSize)
            CompressionType.BZIP2 -> BZIP2Compressor.decompress(decompressed, decompressed.size, archiveSector.data, compressedSize, 9)
            CompressionType.GZIP -> if (!GZIPCompressor.inflate(inputStream, decompressed)) return ByteArray(0)
            CompressionType.LZMA -> decompressed = LZMACompressor.decompress(inputStream, decompressedSize)
        }
        return decompressed
    }

}