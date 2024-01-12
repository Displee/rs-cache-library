package com.displee.compress.type

import com.displee.io.impl.InputBuffer

object EmptyCompressor : Compressor {
    override fun compress(bytes: ByteArray): ByteArray {
        return bytes
    }

    override fun decompress(buffer: InputBuffer, compressedSize: Int, decompressedSize: Int): ByteArray {
        return buffer.readBytes(decompressedSize)
    }
}