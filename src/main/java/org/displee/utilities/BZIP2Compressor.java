package org.displee.utilities;

import org.apache.tools.bzip2.CBZip2OutputStream;

import java.io.*;

/**
 * A class representing the BZIP2 (de)compressor.
 * @author Jagex
 * @author Displee
 */
public class BZIP2Compressor {

	/**
	 * The current bzip2 block entry.
	 */
	public static BZIP2BlockEntry bzip2BlockEntry = new BZIP2BlockEntry();

	/**
	 * An unkown integer array.
	 */
	private static int[] anIntArray5786;

	/**
	 * Compress a decompressed BZIP2 file.
	 * @param bytes The uncompressed BZIP2 file.
	 * @return The compressed BZIP2 file.
	 */
	public static byte[] compress(byte[] bytes) {
		try {
			try (InputStream is = new ByteArrayInputStream(bytes)) {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				try (OutputStream os = new CBZip2OutputStream(bout, 1)) {
					byte[] buf = new byte[4096];
					int len;
					while ((len = is.read(buf, 0, buf.length)) != -1) {
						os.write(buf, 0, len);
					}
				}
				bytes = bout.toByteArray();
				byte[] bzip2 = new byte[bytes.length - 2];
				System.arraycopy(bytes, 2, bzip2, 0, bzip2.length);
				return bzip2;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] decompress317(int decompressedLength, int compressedLength, org.displee.io.impl.InputStream inputStream) {
		byte[] decompressed = new byte[decompressedLength];
		byte[] compressed = inputStream.readBytes(compressedLength);
		decompress(decompressed, decompressed.length, compressed, compressed.length, 0);
		return decompressed;
	}

	/**
	 * Decompress a compressed BZIP2 file.
	 * @param decompressed An empty byte array where we put the decompressed data in.
	 * @param decompressedLength The length to decompress.
	 * @param archiveData The compressed BZIP2 file.
	 * @param compressedSize The size of the compressed BZIP2 file.
	 * @param startOffset The start offset.
	 * @return The decompressed length.
	 */
	public static int decompress(byte[] decompressed, int decompressedLength, byte[] archiveData, int compressedSize, int startOffset) {
		synchronized (bzip2BlockEntry) {
			bzip2BlockEntry.compressed = archiveData;
			bzip2BlockEntry.startOffset = startOffset;
			bzip2BlockEntry.decompressed = decompressed;
			bzip2BlockEntry.anInt3100 = 0;
			bzip2BlockEntry.decompressedLength = decompressedLength;
			bzip2BlockEntry.anInt3088 = 0;
			bzip2BlockEntry.anInt3078 = 0;
			bzip2BlockEntry.anInt3085 = 0;
			bzip2BlockEntry.anInt3097 = 0;
			decompress(bzip2BlockEntry);
			decompressedLength -= bzip2BlockEntry.decompressedLength;
			bzip2BlockEntry.compressed = null;
			bzip2BlockEntry.decompressed = null;
			return decompressedLength;
		}
	}

	/**
	 * Decompress a BZIP2 block entry.
	 * @param bzip2BlockEntry {@link BZIP2BlockEntry}
	 */
	public static void decompress(BZIP2BlockEntry bzip2BlockEntry) {
		boolean bool = false;
		boolean bool_9_ = false;
		boolean bool_10_ = false;
		boolean bool_11_ = false;
		boolean bool_12_ = false;
		boolean bool_13_ = false;
		boolean bool_14_ = false;
		boolean bool_15_ = false;
		boolean bool_16_ = false;
		boolean bool_17_ = false;
		boolean bool_18_ = false;
		boolean bool_19_ = false;
		boolean bool_20_ = false;
		boolean bool_21_ = false;
		boolean bool_22_ = false;
		boolean bool_23_ = false;
		boolean bool_24_ = false;
		boolean bool_25_ = false;
		int i = 0;
		int[] is = null;
		int[] is_26_ = null;
		int[] is_27_ = null;
		bzip2BlockEntry.anInt3096 = 1;
		if (anIntArray5786 == null) {
			anIntArray5786 = new int[bzip2BlockEntry.anInt3096 * 100000];
		}
		boolean bool_28_ = true;
		while (bool_28_) {
			byte i_29_ = method147(bzip2BlockEntry);
			if (i_29_ == 23) {
				break;
			}
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method147(bzip2BlockEntry);
			i_29_ = method153(bzip2BlockEntry);
			bzip2BlockEntry.anInt3083 = 0;
			int i_30_ = method147(bzip2BlockEntry);
			bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 << 8 | i_30_ & 0xff;
			i_30_ = method147(bzip2BlockEntry);
			bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 << 8 | i_30_ & 0xff;
			i_30_ = method147(bzip2BlockEntry);
			bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 << 8 | i_30_ & 0xff;
			for (int i_31_ = 0; i_31_ < 16; i_31_++) {
				i_29_ = method153(bzip2BlockEntry);
				if (i_29_ == 1) {
					bzip2BlockEntry.aBooleanArray3072[i_31_] = true;
				} else {
					bzip2BlockEntry.aBooleanArray3072[i_31_] = false;
				}
			}
			for (int i_32_ = 0; i_32_ < 256; i_32_++) {
				bzip2BlockEntry.aBooleanArray3103[i_32_] = false;
			}
			for (int i_33_ = 0; i_33_ < 16; i_33_++) {
				if (bzip2BlockEntry.aBooleanArray3072[i_33_]) {
					for (int i_34_ = 0; i_34_ < 16; i_34_++) {
						i_29_ = method153(bzip2BlockEntry);
						if (i_29_ == 1) {
							bzip2BlockEntry.aBooleanArray3103[i_33_ * 16 + i_34_] = true;
						}
					}
				}
			}
			method149(bzip2BlockEntry);
			int i_35_ = bzip2BlockEntry.anInt3073 + 2;
			int i_36_ = method152(3, bzip2BlockEntry);
			int i_37_ = method152(15, bzip2BlockEntry);
			for (int i_38_ = 0; i_38_ < i_37_; i_38_++) {
				int i_39_ = 0;
				for (;;) {
					i_29_ = method153(bzip2BlockEntry);
					if (i_29_ == 0) {
						break;
					}
					i_39_++;
				}
				bzip2BlockEntry.aByteArray3094[i_38_] = (byte) i_39_;
			}
			byte[] is_40_ = new byte[6];
			for (byte i_41_ = 0; i_41_ < i_36_; i_41_++) {
				is_40_[i_41_] = i_41_;
			}
			for (int i_42_ = 0; i_42_ < i_37_; i_42_++) {
				byte i_43_ = bzip2BlockEntry.aByteArray3094[i_42_];
				byte i_44_ = is_40_[i_43_];
				for (/**/; i_43_ > 0; i_43_--) {
					is_40_[i_43_] = is_40_[i_43_ - 1];
				}
				is_40_[0] = i_44_;
				bzip2BlockEntry.aByteArray3076[i_42_] = i_44_;
			}
			for (int i_45_ = 0; i_45_ < i_36_; i_45_++) {
				int i_46_ = method152(5, bzip2BlockEntry);
				for (int i_47_ = 0; i_47_ < i_35_; i_47_++) {
					for (;;) {
						i_29_ = method153(bzip2BlockEntry);
						if (i_29_ == 0) {
							break;
						}
						i_29_ = method153(bzip2BlockEntry);
						if (i_29_ == 0) {
							i_46_++;
						} else {
							i_46_--;
						}
					}
					bzip2BlockEntry.aByteArrayArray3098[i_45_][i_47_] = (byte) i_46_;
				}
			}
			for (int i_48_ = 0; i_48_ < i_36_; i_48_++) {
				int i_49_ = 32;
				byte i_50_ = 0;
				for (int i_51_ = 0; i_51_ < i_35_; i_51_++) {
					if (bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_] > i_50_) {
						i_50_ = bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_];
					}
					if (bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_] < i_49_) {
						i_49_ = bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_];
					}
				}
				method145(bzip2BlockEntry.anIntArrayArray3095[i_48_],
						bzip2BlockEntry.anIntArrayArray3082[i_48_],
						bzip2BlockEntry.anIntArrayArray3099[i_48_],
						bzip2BlockEntry.aByteArrayArray3098[i_48_], i_49_, i_50_,
						i_35_);
				bzip2BlockEntry.anIntArray3090[i_48_] = i_49_;
			}
			int i_52_ = bzip2BlockEntry.anInt3073 + 1;
			int i_53_ = -1;
			int i_54_ = 0;
			for (int i_55_ = 0; i_55_ <= 255; i_55_++) {
				bzip2BlockEntry.anIntArray3075[i_55_] = 0;
			}
			int i_56_ = 4095;
			for (int i_57_ = 15; i_57_ >= 0; i_57_--) {
				for (int i_58_ = 15; i_58_ >= 0; i_58_--) {
					bzip2BlockEntry.aByteArray3101[i_56_] = (byte) (i_57_ * 16 + i_58_);
					i_56_--;
				}
				bzip2BlockEntry.anIntArray3092[i_57_] = i_56_ + 1;
			}
			int i_59_ = 0;
			if (i_54_ == 0) {
				i_53_++;
				i_54_ = 50;
				byte i_60_ = bzip2BlockEntry.aByteArray3076[i_53_];
				i = bzip2BlockEntry.anIntArray3090[i_60_];
				is = bzip2BlockEntry.anIntArrayArray3095[i_60_];
				is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_60_];
				is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_60_];
			}
			i_54_--;
			int i_61_ = i;
			int i_62_;
			int i_63_;
			for (i_63_ = method152(i_61_, bzip2BlockEntry); i_63_ > is[i_61_];
					i_63_ = i_63_ << 1 | i_62_) {
				i_61_++;
				i_62_ = method153(bzip2BlockEntry);
			}
			int i_64_ = is_27_[i_63_ - is_26_[i_61_]];
			while (i_64_ != i_52_) {
				if (i_64_ == 0 || i_64_ == 1) {
					int i_65_ = -1;
					int i_66_ = 1;
					do {
						if (i_64_ == 0) {
							i_65_ += i_66_;
						} else if (i_64_ == 1) {
							i_65_ += 2 * i_66_;
						}
						i_66_ *= 2;
						if (i_54_ == 0) {
							i_53_++;
							i_54_ = 50;
							byte i_67_ = bzip2BlockEntry.aByteArray3076[i_53_];
							i = bzip2BlockEntry.anIntArray3090[i_67_];
							is = bzip2BlockEntry.anIntArrayArray3095[i_67_];
							is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_67_];
							is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_67_];
						}
						i_54_--;
						i_61_ = i;
						for (i_63_ = method152(i_61_, bzip2BlockEntry); i_63_ > is[i_61_];
								i_63_ = i_63_ << 1 | i_62_) {
							i_61_++;
							i_62_ = method153(bzip2BlockEntry);
						}
						i_64_ = is_27_[i_63_ - is_26_[i_61_]];
					} while (i_64_ == 0 || i_64_ == 1);
					i_65_++;
					i_30_ = (bzip2BlockEntry.aByteArray3107
							[(bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[0]]
									& 0xff)]);
					bzip2BlockEntry.anIntArray3075[i_30_ & 0xff] += i_65_;
					for (/**/; i_65_ > 0; i_65_--) {
						anIntArray5786[i_59_]
								= i_30_ & 0xff;
						i_59_++;
					}
				} else {
					int i_68_ = i_64_ - 1;
					if (i_68_ < 16) {
						int i_69_ = bzip2BlockEntry.anIntArray3092[0];
						i_29_ = bzip2BlockEntry.aByteArray3101[i_69_ + i_68_];
						for (/**/; i_68_ > 3; i_68_ -= 4) {
							int i_70_ = i_69_ + i_68_;
							bzip2BlockEntry.aByteArray3101[i_70_]
									= bzip2BlockEntry.aByteArray3101[i_70_ - 1];
							bzip2BlockEntry.aByteArray3101[i_70_ - 1]
									= bzip2BlockEntry.aByteArray3101[i_70_ - 2];
							bzip2BlockEntry.aByteArray3101[i_70_ - 2]
									= bzip2BlockEntry.aByteArray3101[i_70_ - 3];
							bzip2BlockEntry.aByteArray3101[i_70_ - 3]
									= bzip2BlockEntry.aByteArray3101[i_70_ - 4];
						}
						for (/**/; i_68_ > 0; i_68_--) {
							bzip2BlockEntry.aByteArray3101[i_69_ + i_68_]
									= bzip2BlockEntry.aByteArray3101[i_69_ + i_68_ - 1];
						}
						bzip2BlockEntry.aByteArray3101[i_69_] = i_29_;
					} else {
						int i_71_ = i_68_ / 16;
						int i_72_ = i_68_ % 16;
						int i_73_ = bzip2BlockEntry.anIntArray3092[i_71_] + i_72_;
						i_29_ = bzip2BlockEntry.aByteArray3101[i_73_];
						for (/**/; i_73_ > bzip2BlockEntry.anIntArray3092[i_71_]; i_73_--) {
							bzip2BlockEntry.aByteArray3101[i_73_]
									= bzip2BlockEntry.aByteArray3101[i_73_ - 1];
						}
						bzip2BlockEntry.anIntArray3092[i_71_]++;
						for (/**/; i_71_ > 0; i_71_--) {
							bzip2BlockEntry.anIntArray3092[i_71_]--;
							bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[i_71_]]
									= (bzip2BlockEntry.aByteArray3101
											[bzip2BlockEntry.anIntArray3092[i_71_ - 1] + 16 - 1]);
						}
						bzip2BlockEntry.anIntArray3092[0]--;
						bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[0]] = i_29_;
						if (bzip2BlockEntry.anIntArray3092[0] == 0) {
							int i_74_ = 4095;
							for (int i_75_ = 15; i_75_ >= 0; i_75_--) {
								for (int i_76_ = 15; i_76_ >= 0; i_76_--) {
									bzip2BlockEntry.aByteArray3101[i_74_]
											= (bzip2BlockEntry.aByteArray3101
													[(bzip2BlockEntry.anIntArray3092[i_75_]
															+ i_76_)]);
									i_74_--;
								}
								bzip2BlockEntry.anIntArray3092[i_75_] = i_74_ + 1;
							}
						}
					}
					bzip2BlockEntry.anIntArray3075[(bzip2BlockEntry.aByteArray3107[i_29_ & 0xff]
							& 0xff)]++;
					anIntArray5786[i_59_]
							= bzip2BlockEntry.aByteArray3107[i_29_ & 0xff] & 0xff;
					i_59_++;
					if (i_54_ == 0) {
						i_53_++;
						i_54_ = 50;
						byte i_77_ = bzip2BlockEntry.aByteArray3076[i_53_];
						i = bzip2BlockEntry.anIntArray3090[i_77_];
						is = bzip2BlockEntry.anIntArrayArray3095[i_77_];
						is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_77_];
						is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_77_];
					}
					i_54_--;
					i_61_ = i;
					for (i_63_ = method152(i_61_, bzip2BlockEntry); i_63_ > is[i_61_];
							i_63_ = i_63_ << 1 | i_62_) {
						i_61_++;
						i_62_ = method153(bzip2BlockEntry);
					}
					i_64_ = is_27_[i_63_ - is_26_[i_61_]];
				}
			}
			bzip2BlockEntry.anInt3080 = 0;
			bzip2BlockEntry.aByte3108 = (byte) 0;
			bzip2BlockEntry.anIntArray3091[0] = 0;
			for (int i_78_ = 1; i_78_ <= 256; i_78_++) {
				bzip2BlockEntry.anIntArray3091[i_78_] = bzip2BlockEntry.anIntArray3075[i_78_ - 1];
			}
			for (int i_79_ = 1; i_79_ <= 256; i_79_++) {
				bzip2BlockEntry.anIntArray3091[i_79_] += bzip2BlockEntry.anIntArray3091[i_79_ - 1];
			}
			for (int i_80_ = 0; i_80_ < i_59_; i_80_++) {
				i_30_ = (byte) (anIntArray5786[i_80_]
						& 0xff);
				anIntArray5786[(bzip2BlockEntry.anIntArray3091
						[i_30_ & 0xff])]
								|= i_80_ << 8;
				bzip2BlockEntry.anIntArray3091[i_30_ & 0xff]++;
			}
			bzip2BlockEntry.anInt3106
			= anIntArray5786[bzip2BlockEntry.anInt3083] >> 8;
				bzip2BlockEntry.anInt3071 = 0;
				bzip2BlockEntry.anInt3106
				= anIntArray5786[bzip2BlockEntry.anInt3106];
				bzip2BlockEntry.anInt3070 = (byte) (bzip2BlockEntry.anInt3106 & 0xff);
				bzip2BlockEntry.anInt3106 >>= 8;
			bzip2BlockEntry.anInt3071++;
			bzip2BlockEntry.anInt3077 = i_59_;
			method151(bzip2BlockEntry);
			if (bzip2BlockEntry.anInt3071 == bzip2BlockEntry.anInt3077 + 1 && bzip2BlockEntry.anInt3080 == 0) {
				bool_28_ = true;
			} else {
				bool_28_ = false;
			}
		}
	}

	public static int method152(int arg0, BZIP2BlockEntry arg1) {
		int i;
		for (;;) {
			if (arg1.anInt3088 >= arg0) {
				int i_93_ = (arg1.anInt3078 >> arg1.anInt3088 - arg0
				& (1 << arg0) - 1);
				arg1.anInt3088 -= arg0;
				i = i_93_;
				break;
			}
			arg1.anInt3078 = (arg1.anInt3078 << 8
					| arg1.compressed[arg1.startOffset] & 0xff);
			arg1.anInt3088 += 8;
			arg1.startOffset++;
			arg1.anInt3085++;
		}
		return i;
	}

	public static void method151(BZIP2BlockEntry arg0) {
		byte i = arg0.aByte3108;
		int i_81_ = arg0.anInt3080;
		int i_82_ = arg0.anInt3071;
		int i_83_ = arg0.anInt3070;
		int[] is = anIntArray5786;
		int i_84_ = arg0.anInt3106;
		byte[] is_85_ = arg0.decompressed;
		int i_86_ = arg0.anInt3100;
		int i_87_ = arg0.decompressedLength;
		int i_88_ = i_87_;
		int i_89_ = arg0.anInt3077 + 1;
		while_68_:
			for (;;) {
				if (i_81_ > 0) {
					for (;;) {
						if (i_87_ == 0) {
							break while_68_;
						}
						if (i_81_ == 1) {
							break;
						}
						is_85_[i_86_] = i;
						i_81_--;
						i_86_++;
						i_87_--;
					}
					if (i_87_ == 0) {
						i_81_ = 1;
						break;
					}
					is_85_[i_86_] = i;
					i_86_++;
					i_87_--;
				}
				boolean bool = true;
				while (bool) {
					bool = false;
					if (i_82_ == i_89_) {
						i_81_ = 0;
						break while_68_;
					}
					i = (byte) i_83_;
					i_84_ = is[i_84_];
					int i_90_ = (byte) (i_84_ & 0xff);
					i_84_ >>= 8;
			i_82_++;
			if (i_90_ != i_83_) {
				i_83_ = i_90_;
				if (i_87_ == 0) {
					i_81_ = 1;
					break while_68_;
				}
				is_85_[i_86_] = i;
				i_86_++;
				i_87_--;
				bool = true;
			} else if (i_82_ == i_89_) {
				if (i_87_ == 0) {
					i_81_ = 1;
					break while_68_;
				}
				is_85_[i_86_] = i;
				i_86_++;
				i_87_--;
				bool = true;
			}
				}
				i_81_ = 2;
				i_84_ = is[i_84_];
				int i_91_ = (byte) (i_84_ & 0xff);
				i_84_ >>= 8;
				if (++i_82_ != i_89_) {
					if (i_91_ != i_83_) {
						i_83_ = i_91_;
					} else {
						i_81_ = 3;
						i_84_ = is[i_84_];
						i_91_ = (byte) (i_84_ & 0xff);
						i_84_ >>= 8;
			if (++i_82_ != i_89_) {
				if (i_91_ != i_83_) {
					i_83_ = i_91_;
				} else {
					i_84_ = is[i_84_];
					i_91_ = (byte) (i_84_ & 0xff);
					i_84_ >>= 8;
			i_82_++;
			i_81_ = (i_91_ & 0xff) + 4;
			i_84_ = is[i_84_];
			i_83_ = (byte) (i_84_ & 0xff);
			i_84_ >>= 8;
			i_82_++;
				}
			}
					}
				}
			}
		int i_92_ = arg0.anInt3097;
		arg0.anInt3097 += i_88_ - i_87_;
		arg0.aByte3108 = i;
		arg0.anInt3080 = i_81_;
		arg0.anInt3071 = i_82_;
		arg0.anInt3070 = i_83_;
		anIntArray5786 = is;
		arg0.anInt3106 = i_84_;
		arg0.decompressed = is_85_;
		arg0.anInt3100 = i_86_;
		arg0.decompressedLength = i_87_;
	}

	public static void method145(int[] arg0, int[] arg1, int[] arg2,
			byte[] arg3, int arg4, int arg5, int arg6) {
		int i = 0;
		for (int i_0_ = arg4; i_0_ <= arg5; i_0_++) {
			for (int i_1_ = 0; i_1_ < arg6; i_1_++) {
				if (arg3[i_1_] == i_0_) {
					arg2[i] = i_1_;
					i++;
				}
			}
		}
		for (int i_2_ = 0; i_2_ < 23; i_2_++) {
			arg1[i_2_] = 0;
		}
		for (int i_3_ = 0; i_3_ < arg6; i_3_++) {
			arg1[arg3[i_3_] + 1]++;
		}
		for (int i_4_ = 1; i_4_ < 23; i_4_++) {
			arg1[i_4_] += arg1[i_4_ - 1];
		}
		for (int i_5_ = 0; i_5_ < 23; i_5_++) {
			arg0[i_5_] = 0;
		}
		int i_6_ = 0;
		for (int i_7_ = arg4; i_7_ <= arg5; i_7_++) {
			i_6_ += arg1[i_7_ + 1] - arg1[i_7_];
			arg0[i_7_] = i_6_ - 1;
			i_6_ <<= 1;
		}
		for (int i_8_ = arg4 + 1; i_8_ <= arg5; i_8_++) {
			arg1[i_8_] = (arg0[i_8_ - 1] + 1 << 1) - arg1[i_8_];
		}
	}

	public static byte method147(BZIP2BlockEntry arg0) {
		return (byte) method152(8, arg0);
	}

	public static void method148() {
		bzip2BlockEntry = null;
	}

	public static void method149(BZIP2BlockEntry arg0) {
		arg0.anInt3073 = 0;
		for (int i = 0; i < 256; i++) {
			if (arg0.aBooleanArray3103[i]) {
				arg0.aByteArray3107[arg0.anInt3073] = (byte) i;
				arg0.anInt3073++;
			}
		}
	}

	public static byte method153(BZIP2BlockEntry arg0) {
		return (byte) method152(1, arg0);
	}

	public static class BZIP2BlockEntry {

		public int anInt3070;
		public int anInt3071;
		public boolean[] aBooleanArray3072;
		public int anInt3073;
		public int startOffset;
		public int[] anIntArray3075;
		public byte[] aByteArray3076;
		public int anInt3077;
		public int anInt3078;
		public byte[] decompressed;
		public int anInt3080;
		public int anInt3081 = 0;
		public int[][] anIntArrayArray3082 = new int[6][258];
		public int anInt3083;
		public int[] anIntArray3084;
		public int anInt3085;
		public int[] anIntArray3086 = { 2, 1, 1, 1, 2, 2, 2, 1, 3, 3, 3, 2, 0, 4, 0 };
		public int anInt3087;
		public int anInt3088;
		public int anInt3089;
		public int[] anIntArray3090;
		public int[] anIntArray3091;
		public int[] anIntArray3092;
		public int decompressedLength;
		public byte[] aByteArray3094 = new byte[18002];
		public int[][] anIntArrayArray3095;
		public int anInt3096;
		public int anInt3097;
		public byte[][] aByteArrayArray3098;
		public int[][] anIntArrayArray3099;
		public int anInt3100;
		public byte[] aByteArray3101;
		public int anInt3102;
		public boolean[] aBooleanArray3103;
		public int anInt3105;
		public int anInt3106;
		public byte[] aByteArray3107;
		public byte aByte3108;
		public byte[] compressed;

		public BZIP2BlockEntry() {
			aBooleanArray3072 = new boolean[16];
			anIntArray3090 = new int[6];
			startOffset = 0;
			anInt3100 = 0;
			aByteArray3101 = new byte[4096];
			anIntArray3091 = new int[257];
			aBooleanArray3103 = new boolean[256];
			anIntArray3075 = new int[256];
			anIntArray3092 = new int[16];
			anIntArrayArray3099 = new int[6][258];
			aByteArray3076 = new byte[18002];
			anIntArrayArray3095 = new int[6][258];
			aByteArrayArray3098 = new byte[6][258];
			aByteArray3107 = new byte[256];
		}

	}

}