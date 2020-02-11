package com.displee.compress;

import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;
import com.displee.util.Utils;
import lzma.sdk.lzma.Decoder;
import lzma.sdk.lzma.Encoder;
import lzma.streams.LzmaEncoderWrapper;

import java.io.*;


/**
 * A class handling LZMA compression.
 * @author Displee (credits to Techdaan)
 */
public class LZMACompressor {

	private static final Decoder DECODER = new Decoder();
	private static final Encoder ENCODER = new Encoder();
	private static final LzmaEncoderWrapper ENCODER_WRAPPER = new LzmaEncoderWrapper(ENCODER) {
		@Override
		public void code(InputStream in, OutputStream out) throws IOException {
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

	public static byte[] compress(byte[] decompressed) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
			lzma.streams.LzmaOutputStream lzma = new lzma.streams.LzmaOutputStream(baos, ENCODER_WRAPPER);
			Utils.INSTANCE.copy(bais, lzma);
			baos.close();
			lzma.close();
			bais.close();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] decompress(InputBuffer buffer, int decompressedLength) {
		OutputBuffer output = new OutputBuffer(buffer.remaining());
		output.write(buffer.raw(), buffer.getOffset(), buffer.remaining());
		return decompress(output.raw(), decompressedLength);
	}

	public static byte[] decompress(byte[] compressed, int decompressedLength) {
		try {
			return decompress(new ByteArrayInputStream(compressed), decompressedLength);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

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