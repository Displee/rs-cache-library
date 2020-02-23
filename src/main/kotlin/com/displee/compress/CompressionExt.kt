package com.displee.compress

import com.displee.cache.index.archive.ArchiveSector
import com.displee.cache.compress.type.BZIP2Compressor
import com.displee.compress.type.GZIPCompressor
import com.displee.compress.type.LZMACompressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer

fun ByteArray.compress(compressionType: CompressionType, xteas: IntArray? = null, revision: Int = -1): ByteArray {
    val compressed: ByteArray = when (compressionType) {
        CompressionType.NONE -> this
        CompressionType.BZIP2 -> BZIP2Compressor.compress(this)
        CompressionType.GZIP -> GZIPCompressor.deflate(this)
        CompressionType.LZMA -> LZMACompressor.compress(this)
    }
    val buffer = OutputBuffer(9 + compressed.size + if (revision == -1) 0 else 2)
    buffer.writeByte(compressionType.ordinal).writeInt(compressed.size)
    if (compressionType != CompressionType.NONE) {
        buffer.writeInt(size)
    }
    buffer.writeBytes(compressed)
    if (xteas != null && (xteas[0] != 0 || xteas[1] != 0 || xteas[2] != 0 || 0 != xteas[3])) {
        check(compressionType != CompressionType.BZIP2) { "BZIP2 compression doesn't work with xtea encryption." }
        buffer.encryptXTEA(xteas, 5, buffer.offset)
    }
    if (revision != -1) {
        buffer.writeShort(revision)
    }
    return buffer.array()
}

fun ArchiveSector.decompress(keys: IntArray? = null): ByteArray {
    val compressedData = data
    val buffer = InputBuffer(compressedData)
    if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
        buffer.decryptXTEA(keys, 5, compressedData.size)
    }
    val type = buffer.readUnsignedByte()
    val compressionType = CompressionType.compressionTypes[type].also { compressionType = it }
    val compressedSize = buffer.readInt() and 0xFFFFFF
    var decompressedSize = 0
    if (compressionType != CompressionType.NONE) {
        decompressedSize = buffer.readInt() and 0xFFFFFF
    }
    var decompressed = ByteArray(decompressedSize)
    when (compressionType) {
        CompressionType.NONE -> decompressed = buffer.readBytes(compressedSize)
        CompressionType.BZIP2 -> BZIP2Compressor.decompress(decompressed, decompressed.size, compressedData, compressedSize, 9)
        CompressionType.GZIP -> if (!GZIPCompressor.inflate(buffer, decompressed)) return byteArrayOf()
        CompressionType.LZMA -> decompressed = LZMACompressor.decompress(buffer, decompressedSize)
    }
    return decompressed
}