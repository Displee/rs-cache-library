package com.displee.compress.type

import com.displee.compress.CompressionType
import com.displee.io.impl.InputBuffer

interface Compressor {

    fun compress(bytes: ByteArray): ByteArray

    fun decompress(buffer: InputBuffer, compressedData: ByteArray, compressedSize: Int, decompressedSize: Int, offset: Int = 9): ByteArray

    companion object {
        fun get(type: CompressionType): Compressor {
            return when (type) {
                CompressionType.NONE -> EmptyCompressor
                CompressionType.BZIP2 -> BZIP2Compressor
                CompressionType.GZIP -> GZIPCompressor
                CompressionType.LZMA -> LZMACompressor
            }
        }
    }
}