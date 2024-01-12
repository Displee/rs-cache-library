package com.displee.compress.type

import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.writeTo
import lzma.sdk.lzma.Decoder
import lzma.sdk.lzma.Encoder
import lzma.streams.LzmaEncoderWrapper
import lzma.streams.LzmaOutputStream
import java.io.*

/**
 * A class handling LZMA compression.
 * @author Displee (credits to Techdaan)
 */
class LZMACompressor : Compressor {

    private val DECODER = Decoder()
    private val ENCODER = Encoder()
    private val ENCODER_WRAPPER: LzmaEncoderWrapper = object : LzmaEncoderWrapper(ENCODER) {
        @Throws(IOException::class)
        override fun code(inputStream: InputStream, outputStream: OutputStream) {
            ENCODER.writeCoderProperties(outputStream)
            ENCODER.code(inputStream, outputStream, -1, -1, null)
        }
    }

    init {
        ENCODER.setDictionarySize(8 shl 20)
        ENCODER.setNumFastBytes(110)
        ENCODER.setMatchFinder(0)
        ENCODER.setLcLpPb(3, 0, 2)
        ENCODER.setEndMarkerMode(false)
    }

    override fun compress(bytes: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            val bais = ByteArrayInputStream(bytes)
            val lzma = LzmaOutputStream(baos, ENCODER_WRAPPER)
            bais.writeTo(lzma)
            baos.close()
            lzma.close()
            bais.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return baos.toByteArray()
    }

    override fun decompress(buffer: InputBuffer, compressedSize: Int, decompressedSize: Int): ByteArray {
        val output = OutputBuffer(buffer.remaining())
        output.writeBytes(buffer.raw(), buffer.offset, buffer.remaining())
        return try {
            decompress(ByteArrayInputStream(output.raw()), decompressedSize)
        } catch (e: IOException) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    @Throws(IOException::class)
    fun decompress(input: ByteArrayInputStream, decompressedLength: Int): ByteArray {
        val properties = ByteArray(5)
        if (input.read(properties) != 5) {
            throw IOException("LZMA: Bad input.")
        }
        val output = ByteArrayOutputStream(decompressedLength)
        synchronized(DECODER) {
            if (!DECODER.setDecoderProperties(properties)) {
                throw IOException("LZMA: Bad properties.")
            }
            DECODER.code(input, output, decompressedLength.toLong())
        }
        output.flush()
        return output.toByteArray()
    }

}