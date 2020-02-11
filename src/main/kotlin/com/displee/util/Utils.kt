package com.displee.util

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Utils {

    private val CRC_TABLE = IntArray(256)

    init {
        for (i in 0..255) {
            var crc = i
            for (i_84_ in 0..7) {
                crc = if (crc and 0x1 == 1) {
                    crc ushr 1 xor 0x12477cdf.inv()
                } else {
                    crc ushr 1
                }
            }
            CRC_TABLE[i] = crc
        }
    }

    fun generateCrc(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
        var crc = -1
        for (i in offset until length) {
            crc = crc ushr 8 xor CRC_TABLE[crc xor data[i].toInt() and 0xff]
        }
        crc = crc xor -0x1
        return crc
    }

    @Throws(IOException::class)
    fun copy(from: InputStream, to: OutputStream): Long {
        val buf = ByteArray(0x1000)
        var total: Long = 0
        while (true) {
            val r = from.read(buf)
            if (r == -1) {
                break
            }
            to.write(buf, 0, r)
            total += r.toLong()
        }
        return total
    }

}

fun String.hashCode317(): Int {
    val upperCaseString = toUpperCase()
    var hash = 0
    for (element in upperCaseString) {
        hash = hash * 61 + element.toInt() - 32
    }
    return hash
}