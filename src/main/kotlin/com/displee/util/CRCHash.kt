package com.displee.util

import java.util.zip.CRC32

object CRCHash {

    private val CRC = CRC32()

    fun generate(data: ByteArray): Int {
        return generate(data, 0, data.size)
    }

    fun generate(data: ByteArray, offset: Int, length: Int): Int {
        CRC.update(data, offset, length)
        val hash = CRC.value.toInt()
        CRC.reset()
        return hash
    }

}