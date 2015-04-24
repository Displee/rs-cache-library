package org.displee.utilities;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import org.displee.io.impl.InputStream;

/**
 * A class that represents the gzip (de)compressor.
 * @author Displee
 */
public class GZIPCompressor {

	/**
	 * The inflater.
	 */
	private static Inflater inflater;

	/**
	 * Inflate a deflated GZIP file.
	 * @param inputStream {@link InputStream}
	 * @param data The delfated GZIP file.
	 */
	public static boolean inflate(InputStream inputStream, byte[] data) {
		try {
			if ((inputStream.getBytes()[inputStream.getOffset()] ^ 0xffffffff) != -32 || inputStream.getBytes()[inputStream.getOffset() + 1] != -117) {
				return false;
			}
			if (inflater == null) {
				inflater = new Inflater(true);
			}
			try {
				inflater.setInput(inputStream.getBytes(), 10 + inputStream.getOffset(), (-10 - inputStream.getOffset() - (8 - inputStream.getBytes().length)));
				inflater.inflate(data);
			} catch (Exception exception) {
				inflater.reset();
				return false;
			}
			inflater.reset();
		} catch (RuntimeException runtimeException) {
			runtimeException.printStackTrace();
		}
		return true;
	}

	/**
	 * Deflate an inflated GZIP file.
	 * @param data The infalted GZIP file.
	 * @return The defalted GZIP file.
	 */
	public static final byte[] deflate(byte[] data) {
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

}