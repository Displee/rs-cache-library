package com.displee.compress.type

import com.displee.io.impl.InputBuffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater

class GZIPCompressor : Compressor {

    private var inflater: Inflater? = null
    private val gzipBuffer = ByteArray(1000_000) //because in 317 Jagex stores a lot of data in one file

    override fun decompress(buffer: InputBuffer, compressedData: ByteArray, compressedSize: Int, decompressedSize: Int, offset: Int): ByteArray {
        val decompressed = ByteArray(decompressedSize)
        if (!inflate(buffer, decompressed)) {
            return byteArrayOf()
        }
        return decompressed
    }

    fun inflate(buffer: InputBuffer, data: ByteArray): Boolean {
        val bytes = buffer.raw()
        val offset = buffer.offset
        if (bytes[offset].toInt() != 31 || bytes[offset + 1].toInt() != -117) {
            return false
        }
        val inflater = this.inflater ?: Inflater(true).also { inflater = it }
        try {
            inflater.setInput(bytes, offset + 10, bytes.size - (10 + offset + 8))
            inflater.inflate(data)
        } catch (exception: Exception) {
            inflater.reset()
            return false
        }
        inflater.reset()
        return true
    }

    override fun compress(bytes: ByteArray): ByteArray {
        val compressed = ByteArrayOutputStream()
        try {
            val gzipOutputStream = GZIPOutputStream(compressed)
            gzipOutputStream.write(bytes)
            gzipOutputStream.finish()
            gzipOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return compressed.toByteArray()
    }

    fun deflate317(data: ByteArray?): ByteArray {
        var read = 0
        try {
            val gis = GZIPInputStream(ByteArrayInputStream(data))
            while (true) {
                if (read == gzipBuffer.size) {
                    throw RuntimeException("buffer overflow!")
                }
                val length = gis.read(gzipBuffer, read, gzipBuffer.size - read)
                if (length == -1) {
                    break
                }
                read += length
            }
        } catch (ex: IOException) {
            throw RuntimeException("error unzipping")
        }
        val deflated = ByteArray(read)
        System.arraycopy(gzipBuffer, 0, deflated, 0, deflated.size)
        return deflated
    }

}