/* Whirlpool - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package org.displee.utilities;
import java.util.Arrays;

public class Whirlpool
{
	public static final int DIGESTBITS = 512;
	public static final int DIGESTBYTES = 64;
	protected static final int anInt7 = 10;
	private static final String sbox
	= "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886";
	private static long[][] aLongArrayArray8 = new long[8][256];
	private static long[] aLongArray9 = new long[11];
	protected byte[] bitLength = new byte[32];
	protected byte[] buffer = new byte[64];
	protected int bufferBits = 0;
	protected int bufferPos = 0;
	protected long[] hash = new long[8];
	protected long[] aLongArray10 = new long[8];
	protected long[] aLongArray11 = new long[8];
	protected long[] block = new long[8];
	protected long[] state = new long[8];

	static {
		for (int x = 0; x < 256; x++) {
			char c
			= "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886"
			.charAt(x / 2);
			long v1 = (x & 0x1) == 0 ? c >>> 8 : c & 0xff;
		long v2 = v1 << 1;
		if (v2 >= 256L) {
			v2 ^= 0x11dL;
		}
		long v4 = v2 << 1;
		if (v4 >= 256L) {
			v4 ^= 0x11dL;
		}
		long v5 = v4 ^ v1;
		long v8 = v4 << 1;
		if (v8 >= 256L) {
			v8 ^= 0x11dL;
		}
		long v9 = v8 ^ v1;
		aLongArrayArray8[0][x] = (v1 << 56 | v1 << 48 | v4 << 40 | v1 << 32
				| v8 << 24 | v5 << 16 | v2 << 8 | v9);
		for (int t = 1; t < 8; t++) {
			aLongArrayArray8[t][x] = (aLongArrayArray8[t - 1][x] >>> 8
			| aLongArrayArray8[t - 1][x] << 56);
		}
		}
		aLongArray9[0] = 0L;
		for (int r = 1; r <= 10; r++) {
			int i = 8 * (r - 1);
			aLongArray9[r] = (aLongArrayArray8[0][i] & ~0xffffffffffffffL
					^ aLongArrayArray8[1][i + 1] & 0xff000000000000L
					^ aLongArrayArray8[2][i + 2] & 0xff0000000000L
					^ aLongArrayArray8[3][i + 3] & 0xff00000000L
					^ aLongArrayArray8[4][i + 4] & 0xff000000L
					^ aLongArrayArray8[5][i + 5] & 0xff0000L
					^ aLongArrayArray8[6][i + 6] & 0xff00L
					^ aLongArrayArray8[7][i + 7] & 0xffL);
		}
	}

	public static byte[] getHash(byte[] data) {
		return getHash(data, 0, data.length);
	}

	public static byte[] getHash(byte[] data, int off, int len) {
		byte[] source;
		if (off <= 0) {
			source = data;
		} else {
			source = new byte[len];
			for (int i = 0; i < len; i++) {
				source[i] = data[off + i];
			}
		}
		Whirlpool whirlpool = new Whirlpool();
		whirlpool.NESSIEinit();
		whirlpool.NESSIEadd(source, len * 8);
		byte[] digest = new byte[64];
		whirlpool.NESSIEfinalize(digest);
		return digest;
	}

	protected void processBuffer() {
		int i = 0;
		int j = 0;
		while (i < 8) {
			block[i] = ((long) buffer[j] << 56
					^ (buffer[j + 1] & 0xffL) << 48
					^ (buffer[j + 2] & 0xffL) << 40
					^ (buffer[j + 3] & 0xffL) << 32
					^ (buffer[j + 4] & 0xffL) << 24
					^ (buffer[j + 5] & 0xffL) << 16
					^ (buffer[j + 6] & 0xffL) << 8
					^ buffer[j + 7] & 0xffL);
			i++;
			j += 8;
		}
		for (i = 0; i < 8; i++) {
			state[i] = block[i] ^ (aLongArray10[i] = hash[i]);
		}
		for (int r = 1; r <= 10; r++) {
			for (int i_0_ = 0; i_0_ < 8; i_0_++) {
				aLongArray11[i_0_] = 0L;
				int t = 0;
				int s = 56;
				while (t < 8) {
					aLongArray11[i_0_] ^= (aLongArrayArray8[t]
							[(int) (aLongArray10[i_0_ - t & 0x7]
									>>> s) & 0xff]);
					t++;
					s -= 8;
				}
			}
			for (int i_1_ = 0; i_1_ < 8; i_1_++) {
				aLongArray10[i_1_] = aLongArray11[i_1_];
			}
			aLongArray10[0] ^= aLongArray9[r];
			for (int i_2_ = 0; i_2_ < 8; i_2_++) {
				aLongArray11[i_2_] = aLongArray10[i_2_];
				int t = 0;
				int s = 56;
				while (t < 8) {
					aLongArray11[i_2_]
							^= (aLongArrayArray8[t]
									[(int) (state[i_2_ - t & 0x7] >>> s) & 0xff]);
					t++;
					s -= 8;
				}
			}
			for (int i_3_ = 0; i_3_ < 8; i_3_++) {
				state[i_3_] = aLongArray11[i_3_];
			}
		}
		for (i = 0; i < 8; i++) {
			hash[i] ^= state[i] ^ block[i];
		}
	}

	public void NESSIEinit() {
		Arrays.fill(bitLength, (byte) 0);
		bufferBits = bufferPos = 0;
		buffer[0] = (byte) 0;
		Arrays.fill(hash, 0L);
	}

	public void NESSIEadd(byte[] source, long sourceBits) {
		int sourcePos = 0;
		int sourceGap = 8 - ((int) sourceBits & 0x7) & 0x7;
		int bufferRem = bufferBits & 0x7;
		long value = sourceBits;
		int i = 31;
		int carry = 0;
		for (/**/; i >= 0; i--) {
			carry += (bitLength[i] & 0xff) + ((int) value & 0xff);
			bitLength[i] = (byte) carry;
			carry >>>= 8;
		value >>>= 8;
		}
		while (sourceBits > 8L) {
			int b = (source[sourcePos] << sourceGap & 0xff
					| (source[sourcePos + 1] & 0xff) >>> 8 - sourceGap);
			if (b < 0 || b >= 256) {
				throw new RuntimeException("LOGIC ERROR");
			}
			buffer[bufferPos++] |= b >>> bufferRem;
			bufferBits += 8 - bufferRem;
			if (bufferBits == 512) {
				processBuffer();
				bufferBits = bufferPos = 0;
			}
			buffer[bufferPos] = (byte) (b << 8 - bufferRem & 0xff);
			bufferBits += bufferRem;
			sourceBits -= 8L;
			sourcePos++;
		}
		int b;
		if (sourceBits > 0L) {
			b = source[sourcePos] << sourceGap & 0xff;
			buffer[bufferPos] |= b >>> bufferRem;
		} else {
			b = 0;
		}
		if (bufferRem + sourceBits < 8L) {
			bufferBits += sourceBits;
		} else {
			bufferPos++;
			bufferBits += 8 - bufferRem;
			sourceBits -= 8 - bufferRem;
			if (bufferBits == 512) {
				processBuffer();
				bufferBits = bufferPos = 0;
			}
			buffer[bufferPos] = (byte) (b << 8 - bufferRem & 0xff);
			bufferBits += (int) sourceBits;
		}
	}

	public void NESSIEfinalize(byte[] digest) {
		buffer[bufferPos] |= 128 >>> (bufferBits & 0x7);
			bufferPos++;
			if (bufferPos > 32) {
				while (bufferPos < 64) {
					buffer[bufferPos++] = (byte) 0;
				}
				processBuffer();
				bufferPos = 0;
			}
			while (bufferPos < 32) {
				buffer[bufferPos++] = (byte) 0;
			}
			System.arraycopy(bitLength, 0, buffer, 32, 32);
			processBuffer();
			int i = 0;
			int j = 0;
			while (i < 8) {
				long h = hash[i];
				digest[j] = (byte) (int) (h >>> 56);
				digest[j + 1] = (byte) (int) (h >>> 48);
				digest[j + 2] = (byte) (int) (h >>> 40);
				digest[j + 3] = (byte) (int) (h >>> 32);
				digest[j + 4] = (byte) (int) (h >>> 24);
				digest[j + 5] = (byte) (int) (h >>> 16);
				digest[j + 6] = (byte) (int) (h >>> 8);
				digest[j + 7] = (byte) (int) h;
				i++;
				j += 8;
			}
	}

	public void NESSIEadd(String source) {
		if (source.length() > 0) {
			byte[] data = new byte[source.length()];
			for (int i = 0; i < source.length(); i++) {
				data[i] = (byte) source.charAt(i);
			}
			NESSIEadd(data, 8 * data.length);
		}
	}
}
