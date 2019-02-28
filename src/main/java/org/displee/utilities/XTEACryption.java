package org.displee.utilities;

/**
 * Copyright (c) 2014 RSE Studios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.displee.io.impl.OutputStream;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * An implementation of the XTEA block cipher.
 *
 * @author Graham
 * @author `Discardedx2
 */
public final class XTEACryption {

	/**
	 * The golden ratio.
	 */
	public static final int GOLDEN_RATIO = 0x9E3779B9;

	/**
	 * The number of rounds.
	 */
	public static final int ROUNDS = 32;

	private XTEACryption() {

	}

	/**
	 * Deciphers the specified {@link ByteBuffer} with the given key.
	 *
	 * @param buffer
	 *            The buffer.
	 * @param key
	 *            The key.
	 * @throws IllegalArgumentException
	 *             if the key is not exactly 4 elements long.
	 */
	public static ByteBuffer decipher(ByteBuffer buffer, int start, int end, int[] key) {
		if (key.length != 4)
			throw new IllegalArgumentException();

		int numQuads = (end - start) / 8;
		for (int i = 0; i < numQuads; i++) {
			int sum = GOLDEN_RATIO * ROUNDS;
			int v0 = buffer.getInt(start + i * 8);
			int v1 = buffer.getInt(start + i * 8 + 4);
			for (int j = 0; j < ROUNDS; j++) {
				v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
				sum -= GOLDEN_RATIO;
				v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
			}
			buffer.putInt(start + i * 8, v0);
			buffer.putInt(start + i * 8 + 4, v1);
		}
		return buffer;
	}

	/**
	 * Deciphers the specified {@link ByteBuffer} with the given key.
	 *
	 * @param buffer
	 *            The buffer.
	 * @param key
	 *            The key.
	 * @throws IllegalArgumentException
	 *             if the key is not exactly 4 elements long.
	 */
	public static void decipher(OutputStream buffer, int start, int end, int[] key) {
		if (key.length != 4)
			throw new IllegalArgumentException();

		int numQuads = (end - start) / 8;
		for (int i = 0; i < numQuads; i++) {
			int sum = GOLDEN_RATIO * ROUNDS;
			int v0 = buffer.readInt();
			int v1 = buffer.readInt();
			for (int j = 0; j < ROUNDS; j++) {
				v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
				sum -= GOLDEN_RATIO;
				v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
			}
			buffer.writeInt(v0);
			buffer.writeInt(v1);
		}
	}

	/**
	 * Enciphers the specified {@link ByteBuffer} with the given key.
	 *
	 * @param buffer
	 *            The buffer.
	 * @param key
	 *            The key.
	 * @throws IllegalArgumentException
	 *             if the key is not exactly 4 elements long.
	 */
	public static ByteBuffer encipher(ByteBuffer buffer, int start, int end, int[] key) {
		if (key.length != 4)
			throw new IllegalArgumentException();

		int numQuads = (end - start) / 8;
		for (int i = 0; i < numQuads; i++) {
			int sum = 0;
			int v0 = buffer.getInt(start + i * 8);
			int v1 = buffer.getInt(start + i * 8 + 4);
			for (int j = 0; j < ROUNDS; j++) {
				v0 += (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
				sum += GOLDEN_RATIO;
				v1 += (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
			}
			buffer.putInt(start + i * 8, v0);
			buffer.putInt(start + i * 8 + 4, v1);
		}
		return buffer;
	}

	/**
	 * Enciphers the specified {@link ByteBuffer} with the given key.
	 *
	 * @param buffer
	 *            The buffer.
	 * @param key
	 *            The key.
	 * @throws IllegalArgumentException
	 *             if the key is not exactly 4 elements long.
	 */
	public static void encipher(OutputStream buffer, int start, int end, int[] key) {
		if (key.length != 4)
			throw new IllegalArgumentException();

		int numQuads = (end - start) / 8;
		for (int i = 0; i < numQuads; i++) {
			int sum = 0;
			int v0 = buffer.readInt();
			int v1 = buffer.readInt();
			for (int j = 0; j < ROUNDS; j++) {
				v0 += (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
				sum += GOLDEN_RATIO;
				v1 += (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
			}
			buffer.writeInt(v0);
			buffer.writeInt(v1);
		}
	}

}
