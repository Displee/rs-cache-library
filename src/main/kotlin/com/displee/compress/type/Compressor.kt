package com.displee.compress.type

import com.displee.io.impl.InputBuffer

interface Compressor {

    fun compress(bytes: ByteArray): ByteArray

    fun decompress(buffer: InputBuffer, compressedSize: Int, decompressedSize: Int): ByteArray

}