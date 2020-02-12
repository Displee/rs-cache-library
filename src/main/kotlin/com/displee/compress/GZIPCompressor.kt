package com.displee.compress;

import com.displee.io.impl.InputBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class GZIPCompressor {

	private static Inflater inflater;
	private static byte[] gzipBuffer = new byte[1000000];

	public static boolean inflate(InputBuffer buffer, byte[] data) {
		byte[] bytes = buffer.raw();
		int offset = buffer.getOffset();
		if (bytes[offset] != 31 || bytes[offset + 1] != -117) {
			return false;
		}
		if (inflater == null) {
			inflater = new Inflater(true);
		}
		try {
			inflater.setInput(bytes, offset + 10, bytes.length - (10 + offset + 8));
			inflater.inflate(data);
		} catch (Exception exception) {
			inflater.reset();
			return false;
		}
		inflater.reset();
		return true;
	}

	public static byte[] inflate317(byte[] uncompressed) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try (DeflaterOutputStream os = new GZIPOutputStream(bout)) {
				os.write(uncompressed);
				os.finish();
				return bout.toByteArray();
			}
		} catch (IOException e) {
			return null;
		}
	}

	public static byte[] deflate(byte[] data) {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressed);
			gzipOutputStream.write(data);
			gzipOutputStream.finish();
			gzipOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressed.toByteArray();
	}

	public static byte[] deflate317(byte[] data) {
		int read = 0;
		try {
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
			do {
				if (read == gzipBuffer.length) {
					throw new RuntimeException("buffer overflow!");
				}
				int in = gis.read(gzipBuffer, read, gzipBuffer.length - read);
				if (in == -1) {
					break;
				}
				read += in;
			} while (true);
		} catch (IOException ex) {
			throw new RuntimeException("error unzipping");
		}
		byte[] deflated = new byte[read];
		System.arraycopy(gzipBuffer, 0, deflated, 0, deflated.length);
		return deflated;
	}

}