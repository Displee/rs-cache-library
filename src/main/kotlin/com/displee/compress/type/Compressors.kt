package com.displee.compress.type

import com.displee.compress.CompressionType

class Compressors {
    val bzip2: BZIP2Compressor by lazy { BZIP2Compressor() }
    val gzip: GZIPCompressor by lazy { GZIPCompressor() }
    private val lzma: LZMACompressor by lazy { LZMACompressor() }

    fun get(type: CompressionType): Compressor {
        return when (type) {
            CompressionType.NONE -> EmptyCompressor
            CompressionType.BZIP2 -> bzip2
            CompressionType.GZIP -> gzip
            CompressionType.LZMA -> lzma
        }
    }
}