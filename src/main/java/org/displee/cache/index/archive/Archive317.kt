package org.displee.cache.index.archive

import org.displee.cache.index.archive.file.File
import org.displee.io.impl.InputStream
import org.displee.io.impl.OutputStream
import org.displee.utilities.BZIP2Compressor
import org.displee.utilities.Compression.CompressionType
import org.displee.utilities.GZIPCompressor

class Archive317 : Archive {

    private var extracted = false
    var compressionType: CompressionType? = null
    var priority = 0

    constructor(id: Int) : super(id)
    constructor(id: Int, name: Int) : super(id, name)

    override fun read(inputStream: InputStream): Boolean {
        read = true
        if (compressionType == CompressionType.GZIP) {
            first()?.data = GZIPCompressor.deflate317(inputStream.bytes)
            return true
        }
        val decompressed: ByteArray
        var decompressedLength = inputStream.read24BitInt()
        var compressedLength = inputStream.read24BitInt()
        if (decompressedLength != compressedLength) {
            decompressed = BZIP2Compressor.decompress317(decompressedLength, compressedLength, inputStream)
            extracted = true
        } else {
            decompressed = inputStream.readBytes(inputStream.remaining)
        }
        val stream = InputStream(decompressed)
        val filesLength = stream.readUnsignedShort()
        val filesInputStream = InputStream(stream.bytes)
        filesInputStream.offset = stream.offset + filesLength * 10
        for (i in 0 until filesLength) {
            val fileName = stream.readInt()
            decompressedLength = stream.read24BitInt()
            compressedLength = stream.read24BitInt()
            val data: ByteArray = if (extracted) {
                filesInputStream.readBytes(decompressedLength)
            } else {
                BZIP2Compressor.decompress317(decompressedLength, compressedLength, filesInputStream)
            }
            files[i] = File(i, data, fileName)
        }
        return true
    }

    override fun write(): ByteArray {
        if (compressionType == CompressionType.GZIP) {
            val first = first()
            return GZIPCompressor.inflate317(if (first == null) ByteArray(0) else first.data)
        }
        val outputStream = OutputStream(10 * files.size)
        val files: Collection<File> = files.values
        outputStream.writeShort(files.size)
        val out = OutputStream(2048)
        for (file in files) {
            val fileData = file.data ?: continue
            outputStream.writeInt(file.hashName)
            outputStream.write24BitInt(fileData.size)
            val toWrite = if (extracted) fileData else BZIP2Compressor.compress(file.data)
            outputStream.write24BitInt(toWrite.size)
            out.writeBytes(toWrite)
        }
        outputStream.writeBytes(out.flip())
        val data = outputStream.flip()
        val compressed = if (extracted) BZIP2Compressor.compress(data) else data
        val finalStream = OutputStream(compressed.size + 6)
        finalStream.write24BitInt(data.size)
        finalStream.write24BitInt(compressed.size)
        finalStream.writeBytes(compressed)
        return finalStream.flip()
    }

}