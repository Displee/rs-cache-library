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
    private var gzipBuffer: ByteArray? = null

    override fun decompress(buffer: InputBuffer, compressedSize: Int, decompressedSize: Int): ByteArray {
        return inflate(buffer, decompressedSize)
    }

    private fun inflate(buffer: InputBuffer, decompressedSize: Int): ByteArray {
        val bytes = buffer.raw()
        val offset = buffer.offset
        if (bytes[offset].toInt() != 31 || bytes[offset + 1].toInt() != -117) {
            return byteArrayOf()
        }
        val inflater = this.inflater ?: Inflater(true).also { inflater = it }
        return try {
            inflater.setInput(bytes, offset + 10, bytes.size - (10 + offset + 8))
            val decompressed = ByteArray(decompressedSize)
            inflater.inflate(decompressed)
            decompressed
        } catch (exception: Exception) {
            byteArrayOf()
        } finally {
            inflater.reset()
        }
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
            var gzipBuffer = gzipBuffer
            if (gzipBuffer == null) {
                gzipBuffer = ByteArray(1_000_000) //because in 317 Jagex stores a lot of data in one file
                this.gzipBuffer = gzipBuffer
            }
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