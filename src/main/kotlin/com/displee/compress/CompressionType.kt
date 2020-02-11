package com.displee.compress

enum class CompressionType {
    NONE,
    BZIP2,
    GZIP,
    LZMA;

    companion object {
        val compressionTypes = values()
    }
}