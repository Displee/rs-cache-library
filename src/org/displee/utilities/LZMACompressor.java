package org.displee.utilities;

import lzma.sdk.lzma.Decoder;
import lzma.sdk.lzma.Encoder;
import lzma.streams.LzmaEncoderWrapper;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;

import java.io.*;

/**
 * A class handling LZMA compression.
 * @author Displee (credits to Techdaan)
 */
public class LZMACompressor {

	/**
	 * The decoder.
	 */
	private static final Decoder DECODER = new Decoder();

	/**
	 * The encoder.
	 */
	private static final Encoder ENCODER = new Encoder();

	/**
	 * The encoder wrapper.
	 */
	private static final LzmaEncoderWrapper ENCODER_WRAPPER = new LzmaEncoderWrapper(ENCODER) {
		@Override
		public void code(java.io.InputStream in, java.io.OutputStream out) throws IOException {
			ENCODER.writeCoderProperties(out);
			ENCODER.code(in, out, -1, -1, null);
		}
	};

	static {
		ENCODER.setDictionarySize(8 << 20);
		ENCODER.setNumFastBytes(110);
		ENCODER.setMatchFinder(0);
		ENCODER.setLcLpPb(3, 0, 2);
		ENCODER.setEndMarkerMode(false);
	}

	/**
	 * Compress the decompressed data.
	 * @param decompressed The decompressed data.
	 * @return The compressed data.
	 */
	public static byte[] compress(byte[] decompressed) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
			lzma.streams.LzmaOutputStream lzma = new lzma.streams.LzmaOutputStream(baos, ENCODER_WRAPPER);
			Miscellaneous.copy(bais, lzma);
			baos.close();
			lzma.close();
			bais.close();
			return baos.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Starts the decompression for LZMA.
	 * @param buffer The buffer.
	 * @param decompressedLength The decompressed length.
	 */
	public static byte[] decompress(InputStream buffer, int decompressedLength) {
		OutputStream output = new OutputStream(buffer.getRemaining());
		output.writeBytes(buffer.getBytes(), buffer.getOffset(), buffer.getRemaining());
		return decompress(output.getBytes(), decompressedLength);
	}

	/**
	 * Starts the decompression for LZMA.
	 * @param compressed The compressed data.
	 * @param decompressedLength The decompressed length.
	 */
	public static byte[] decompress(byte[] compressed, int decompressedLength) {
		try {
			return decompress(new ByteArrayInputStream(compressed), decompressedLength);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decompresses data with the LZMA algorithm.
	 * @param input The created input stream wrapped around the initial buffer.
	 * @param decompressedLength The compression length.
	 * @throws IOException
	 */
	public static byte[] decompress(ByteArrayInputStream input, int decompressedLength) throws IOException {
		byte[] properties = new byte[5];
		if (input.read(properties, 0, 5) != 5) {
			throw new IOException("LZMA: Bad input.");
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream(decompressedLength);
		synchronized (DECODER) {
			if (!DECODER.setDecoderProperties(properties)) {
				throw new IOException("LZMA: Bad properties.");
			}
			DECODER.code(input, output, decompressedLength);
		}
		output.flush();
		return output.toByteArray();
	}

}
