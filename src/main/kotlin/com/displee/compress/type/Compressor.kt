package com.displee.compress.type

import com.displee.io.impl.InputBuffer

interface Compressor {

    fun compress(bytes: ByteArray): ByteArray

    fun decompress(buffer: InputBuffer, compressedData: ByteArray, compressedSize: Int, decompressedSize: Int, offset: Int = 9): ByteArray

}