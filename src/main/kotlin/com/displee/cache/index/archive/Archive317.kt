package com.displee.cache.index.archive

import com.displee.cache.index.archive.file.File
import com.displee.compress.BZIP2Compressor
import com.displee.compress.CompressionType
import com.displee.compress.GZIPCompressor
import com.displee.io.impl.InputBuffer
import com.displee.io.impl.OutputBuffer
import com.displee.util.hashCode317

class Archive317 : Archive {

    private var extracted = false
    var priority = 0

    constructor(id: Int) : super(id)

    constructor(id: Int, name: Int) : super(id, name)

    override fun read(buffer: InputBuffer) {
        read = true
        if (compressionType == CompressionType.GZIP) {
            first()?.data = GZIPCompressor.deflate317(buffer.raw())
            return
        }
        val decompressed: ByteArray
        var decompressedLength = buffer.read24BitInt()
        var compressedLength = buffer.read24BitInt()
        if (decompressedLength != compressedLength) {
            decompressed = BZIP2Compressor.decompress317(decompressedLength, compressedLength, buffer)
            extracted = true
        } else {
            decompressed = buffer.read(buffer.remaining())
        }
        val metaBuffer = InputBuffer(decompressed)
        val filesLength = metaBuffer.readUnsignedShort()
        val filesBuffer = InputBuffer(decompressed)
        filesBuffer.offset = metaBuffer.offset + filesLength * 10
        for (i in 0 until filesLength) {
            val fileName = metaBuffer.readInt()
            decompressedLength = metaBuffer.read24BitInt()
            compressedLength = metaBuffer.read24BitInt()
            val data: ByteArray = if (extracted) {
                filesBuffer.read(decompressedLength)
            } else {
                BZIP2Compressor.decompress317(decompressedLength, compressedLength, filesBuffer)
            }
            files[i] = File(i, data, fileName)
        }
    }

    override fun write(): ByteArray {
        if (compressionType == CompressionType.GZIP) {
            return GZIPCompressor.inflate317(first()?.data ?: ByteArray(0))
        }
        val metaBuffer = OutputBuffer(10 * files.size)
        val files: Collection<File> = files.values
        metaBuffer.writeShort(files.size)
        val filesBuffer = OutputBuffer(2048)
        for (file in files) {
            val fileData = file.data ?: continue
            metaBuffer.writeInt(file.hashName)
            metaBuffer.write24BitInt(fileData.size)
            val toWrite = if (extracted) fileData else BZIP2Compressor.compress(file.data)
            metaBuffer.write24BitInt(toWrite.size)
            filesBuffer.write(toWrite)
        }
        metaBuffer.write(filesBuffer.array())
        val uncompressed = metaBuffer.array()
        val compressed = if (extracted) BZIP2Compressor.compress(uncompressed) else uncompressed
        val buffer = OutputBuffer(compressed.size + 6)
        buffer.write24BitInt(uncompressed.size)
        buffer.write24BitInt(compressed.size)
        buffer.write(compressed)
        return buffer.array()
    }

    override fun toHash(name: String): Int {
        return name.hashCode317()
    }

}