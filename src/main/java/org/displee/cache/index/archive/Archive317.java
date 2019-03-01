package org.displee.cache.index.archive;

import org.displee.cache.index.archive.file.File;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.BZIP2Compressor;
import org.displee.utilities.Compression;
import org.displee.utilities.GZIPCompressor;

public class Archive317 extends Archive {

	private boolean extracted;

	private Compression.CompressionType compressionType;

	private int priority;

	public Archive317(int id) {
		super(id);
	}

	public Archive317(int id, int name) {
		super(id, name);
	}

	@Override
	public boolean read(InputStream s) {
		if (compressionType == Compression.CompressionType.GZIP) {
			setFileIds(1);
			setFiles(1);
			getFiles()[0] = new File(0, GZIPCompressor.deflate317(s.getBytes()));
			return read = true;
		}
		byte[] decompressed;
		int decompressedLength = s.read24BitInt();
		int compressedLength = s.read24BitInt();
		if (decompressedLength != compressedLength) {
			decompressed = BZIP2Compressor.decompress317(decompressedLength, compressedLength, s);
			extracted = true;
		} else {
			decompressed = s.readBytes(s.getRemaining());
		}

		InputStream stream = new InputStream(decompressed);
		int filesLength = stream.readUnsignedShort();
		setFileIds(filesLength);
		setFiles(filesLength);
		InputStream filesInputStream = new InputStream(stream.getBytes());
		filesInputStream.setOffset(stream.getOffset() + filesLength * 10);
		for(int i = 0; i < filesLength; i++) {
			int fileName = stream.readInt();
			decompressedLength = stream.read24BitInt();
			compressedLength = stream.read24BitInt();
			byte[] data;
			if (extracted) {
				data = filesInputStream.readBytes(decompressedLength);
			} else {
				data = BZIP2Compressor.decompress317(decompressedLength, compressedLength, filesInputStream);
			}
			getFileIds()[i] = i;
			getFiles()[i] = new File(i, data, fileName);
		}
		return read = true;
	}

	@Override
	public byte[] write(OutputStream outputStream) {
		if (compressionType == Compression.CompressionType.GZIP) {
			return GZIPCompressor.inflate317(getFiles()[0] == null ? new byte[0] : getFiles()[0].getData());
		}
		int length = getFiles().length;
		outputStream.writeShort(length);
		final OutputStream out = new OutputStream(2048);
		for(int i = 0; i < length; i++) {
			File file = getFiles()[i];
			outputStream.writeInt(file.getName());
			outputStream.write24BitInt(file.getData().length);
			byte[] toWrite = extracted ? file.getData() : BZIP2Compressor.compress(file.getData());
			outputStream.write24BitInt(toWrite.length);

			out.writeBytes(toWrite);
		}
		outputStream.writeBytes(out.flip());

		byte[] data = outputStream.flip();

		byte[] compressed = extracted ? BZIP2Compressor.compress(data) : data;

		final OutputStream finalStream = new OutputStream(compressed.length + 6);
		finalStream.write24BitInt(data.length);
		finalStream.write24BitInt(compressed.length);

		finalStream.writeBytes(compressed);
		return finalStream.flip();
	}

	public Compression.CompressionType getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(Compression.CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
