package com.displee.util

import java.io.InputStream
import java.io.OutputStream

private val CRC_TABLE = IntArray(256) {
    var crc = it
    for (i_84_ in 0..7) {
        crc = if (crc and 0x1 == 1) {
            crc ushr 1 xor 0x12477cdf.inv()
        } else {
            crc ushr 1
        }
    }
    crc
}

fun ByteArray.generateCrc(offset: Int = 0, length: Int = size): Int {
    var crc = -1
    for (i in offset until length) {
        crc = crc ushr 8 xor CRC_TABLE[crc xor this[i].toInt() and 0xff]
    }
    crc = crc xor -0x1
    return crc
}

fun InputStream.writeTo(to: OutputStream): Long {
    val buf = ByteArray(0x1000)
    var total: Long = 0
    while (true) {
        val r = read(buf)
        if (r == -1) {
            break
        }
        to.write(buf, 0, r)
        total += r.toLong()
    }
    return total
}

fun String.hashCode317(): Int {
    val upperCaseString = toUpperCase()
    var hash = 0
    for (element in upperCaseString) {
        hash = hash * 61 + element.toInt() - 32
    }
    return hash
}