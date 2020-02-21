package com.displee.util;

public class Whirlpool {

	private static long[][] aLongArrayArray6630 = new long[8][256];
	private static long[] aLongArray6631 = new long[11];

	private long[] block = new long[8];
	private long[] hash = new long[8];
	private long[] aLongArray6637 = new long[8];
	private long[] aLongArray6638 = new long[8];
	private long[] state = new long[8];
	private byte[] bitLength = new byte[32];
	private byte[] buffer = new byte[64];
	private int bufferBits = 0;
	private int bufferPosition = 0;

	static {
		for (int i = 0; i < 256; i++) {
			int i_37_ = "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886".charAt(i / 2);
			long l = ((i & 0x1) == 0 ? (long) (i_37_ >>> 8) : (long) (i_37_ & 0xff));
			long l_38_ = l << 1;
			if (l_38_ >= 256L) {
				l_38_ ^= 0x11dL;
			}
			long l_39_ = l_38_ << 1;
			if (l_39_ >= 256L) {
				l_39_ ^= 0x11dL;
			}
			long l_40_ = l_39_ ^ l;
			long l_41_ = l_39_ << 1;
			if (l_41_ >= 256L) {
				l_41_ ^= 0x11dL;
			}
			long l_42_ = l_41_ ^ l;
			aLongArrayArray6630[0][i] = (l << 56 | l << 48 | l_39_ << 40 | l << 32 | l_41_ << 24 | l_40_ << 16 | l_38_ << 8 | l_42_);
			for (int i_43_ = 1; i_43_ < 8; i_43_++) {
				aLongArrayArray6630[i_43_][i] = (aLongArrayArray6630[i_43_ - 1][i] >>> 8 | aLongArrayArray6630[i_43_ - 1][i] << 56);
			}
		}
		aLongArray6631[0] = 0L;
		for (int i = 1; i <= 10; i++) {
			int i_44_ = 8 * (i - 1);
			aLongArray6631[i] = (aLongArrayArray6630[0][i_44_] & ~0xffffffffffffffL ^ aLongArrayArray6630[1][i_44_ + 1] & 0xff000000000000L ^ aLongArrayArray6630[2][i_44_ + 2] & 0xff0000000000L ^ aLongArrayArray6630[3][i_44_ + 3] & 0xff00000000L ^ aLongArrayArray6630[4][4 + i_44_] & 0xff000000L ^ aLongArrayArray6630[5][5 + i_44_] & 0xff0000L ^ aLongArrayArray6630[6][6 + i_44_] & 0xff00L ^ aLongArrayArray6630[7][7 + i_44_] & 0xffL);
		}
	}

	void NESSIEinit() {
		for (int i = 0; i < 32; i++) {
			bitLength[i] = (byte) 0;
		}
		bufferPosition = 0;
		bufferBits = 0;
		buffer[0] = (byte) 0;
		for (int i_3_ = 0; i_3_ < 8; i_3_++) {
			hash[i_3_] = 0L;
		}
	}

	private void processBuffer() {
		int i_4_ = 0;
		int i_5_ = 0;
		while (i_4_ < 8) {
			block[i_4_] = ((long) buffer[i_5_] << 56 ^ ((long) buffer[i_5_ + 1] & 0xffL) << 48 ^ ((long) buffer[2 + i_5_] & 0xffL) << 40 ^ ((long) buffer[i_5_ + 3] & 0xffL) << 32 ^ ((long) buffer[i_5_ + 4] & 0xffL) << 24 ^ ((long) buffer[5 + i_5_] & 0xffL) << 16 ^ ((long) buffer[i_5_ + 6] & 0xffL) << 8 ^ ((long) buffer[7 + i_5_] & 0xffL));
			i_4_++;
			i_5_ += 8;
		}
		for (i_4_ = 0; i_4_ < 8; i_4_++) {
			state[i_4_] = block[i_4_] ^ (aLongArray6637[i_4_] = hash[i_4_]);
		}
		for (i_4_ = 1; i_4_ <= 10; i_4_++) {
			for (i_5_ = 0; i_5_ < 8; i_5_++) {
				aLongArray6638[i_5_] = 0L;
				int i_6_ = 0;
				int i_7_ = 56;
				while (i_6_ < 8) {
					aLongArray6638[i_5_] ^= (aLongArrayArray6630[i_6_][(int) ((aLongArray6637[i_5_ - i_6_ & 0x7]) >>> i_7_) & 0xff]);
					i_6_++;
					i_7_ -= 8;
				}
			}
			for (i_5_ = 0; i_5_ < 8; i_5_++) {
				aLongArray6637[i_5_] = aLongArray6638[i_5_];
			}
			aLongArray6637[0] ^= aLongArray6631[i_4_];
			for (i_5_ = 0; i_5_ < 8; i_5_++) {
				aLongArray6638[i_5_] = aLongArray6637[i_5_];
				int i_8_ = 0;
				int i_9_ = 56;
				while (i_8_ < 8) {
					aLongArray6638[i_5_] ^= (aLongArrayArray6630[i_8_][(int) ((state[i_5_ - i_8_ & 0x7]) >>> i_9_) & 0xff]);
					i_8_++;
					i_9_ -= 8;
				}
			}
			for (i_5_ = 0; i_5_ < 8; i_5_++) {
				state[i_5_] = aLongArray6638[i_5_];
			}
		}
		for (i_4_ = 0; i_4_ < 8; i_4_++) {
			hash[i_4_] ^= state[i_4_] ^ block[i_4_];
		}
	}

	void NESSIEfinalize(byte[] digest, int i) {
		buffer[(bufferPosition)] |= 128 >>> (bufferBits & 0x7);
		++bufferPosition;
		if (bufferPosition > 32) {
			while (bufferPosition < 64) {
				buffer[bufferPosition++] = (byte) 0;
			}
			processBuffer();
			bufferPosition = 0;
		}
		while (bufferPosition < 32) {
			buffer[bufferPosition++] = (byte) 0;
		}
		System.arraycopy(bitLength, 0, buffer, 32, 32);
		processBuffer();
		int i_16_ = 0;
		int i_17_ = i;
		while (i_16_ < 8) {
			long l = hash[i_16_];
			digest[i_17_] = (byte) (int) (l >>> 56);
			digest[i_17_ + 1] = (byte) (int) (l >>> 48);
			digest[i_17_ + 2] = (byte) (int) (l >>> 40);
			digest[i_17_ + 3] = (byte) (int) (l >>> 32);
			digest[i_17_ + 4] = (byte) (int) (l >>> 24);
			digest[i_17_ + 5] = (byte) (int) (l >>> 16);
			digest[i_17_ + 6] = (byte) (int) (l >>> 8);
			digest[i_17_ + 7] = (byte) (int) l;
			i_16_++;
			i_17_ += 8;
		}
	}

	void NESSIEadd(byte[] source, long sourceBits) {
		int i = 0;
		int i_23_ = 8 - ((int) sourceBits & 0x7) & 0x7;
		int i_24_ = bufferBits & 0x7;
		long l_25_ = sourceBits;
		int i_26_ = 31;
		int i_27_ = 0;
		for (/**/; i_26_ >= 0; i_26_--) {
			i_27_ += ((bitLength[i_26_] & 0xff) + ((int) l_25_ & 0xff));
			bitLength[i_26_] = (byte) i_27_;
			i_27_ >>>= 8;
			l_25_ >>>= 8;
		}
		while (sourceBits > 8L) {
			int i_28_ = source[i] << i_23_ & 0xff | (source[i + 1] & 0xff) >>> 8 - i_23_;
			if (i_28_ < 0 || i_28_ >= 256) {
				throw new RuntimeException();
			}
			buffer[bufferPosition] |= i_28_ >>> i_24_;
			++bufferPosition;
			bufferBits += 8 - i_24_;
			if (bufferBits == 512) {
				processBuffer();
				bufferPosition = 0;
				bufferBits = 0;
			}
			buffer[bufferPosition] = (byte) (i_28_ << 8 - i_24_ & 0xff);
			bufferBits += i_24_;
			sourceBits -= 8L;
			i++;
		}
		int i_29_;
		if (sourceBits > 0L) {
			i_29_ = source[i] << i_23_ & 0xff;
			buffer[bufferPosition] |= i_29_ >>> i_24_;
		} else {
			i_29_ = 0;
		}
		if ((long) i_24_ + sourceBits < 8L) {
			bufferBits += sourceBits;
		} else {
			++bufferPosition;
			bufferBits += 8 - i_24_;
			sourceBits -= (long) (8 - i_24_);
			if (bufferBits == 512) {
				processBuffer();
				bufferPosition = 0;
				bufferBits = 0;
			}
			buffer[bufferPosition] = (byte) (i_29_ << 8 - i_24_ & 0xff);
			bufferBits += (int) sourceBits;
		}
	}

}