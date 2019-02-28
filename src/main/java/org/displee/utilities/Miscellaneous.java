package org.displee.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class containing utilities that are used in this cache library.
 * @author Displee
 */
public class Miscellaneous {

	/**
	 * An array of characters.
	 */
	private static char[] aCharArray6385 = {'€', '\0', '‚', 'ƒ', '„', '…', '†', '‡', 'ˆ', '‰', 'Š', '‹', 'Œ', '\0', 'Ž', '\0', '\0', '‘', '’', '“', '”', '•', '–', '—', '˜', '™', 'š', '›', 'œ', '\0', 'ž', 'Ÿ'};

	/**
	 * Converts a string to an byte array.
	 * @param message The message.
	 * @return The byte array.
	 */
	public static final byte[] getFormattedMessage(String message) {
		int i_0_ = message.length();
		byte[] is = new byte[i_0_];
		for (int i_1_ = 0; (i_1_ ^ 0xffffffff) > (i_0_ ^ 0xffffffff); i_1_++) {
			int i_2_ = message.charAt(i_1_);
			if (((i_2_ ^ 0xffffffff) >= -1 || i_2_ >= 128) && (i_2_ < 160 || i_2_ > 255)) {
				if ((i_2_ ^ 0xffffffff) != -8365) {
					if ((i_2_ ^ 0xffffffff) == -8219) {
						is[i_1_] = (byte) -126;
					} else if ((i_2_ ^ 0xffffffff) == -403) {
						is[i_1_] = (byte) -125;
					} else if (i_2_ == 8222) {
						is[i_1_] = (byte) -124;
					} else if (i_2_ != 8230) {
						if ((i_2_ ^ 0xffffffff) != -8225) {
							if ((i_2_ ^ 0xffffffff) != -8226) {
								if ((i_2_ ^ 0xffffffff) == -711) {
									is[i_1_] = (byte) -120;
								} else if (i_2_ == 8240) {
									is[i_1_] = (byte) -119;
								} else if ((i_2_ ^ 0xffffffff) == -353) {
									is[i_1_] = (byte) -118;
								} else if ((i_2_ ^ 0xffffffff) != -8250) {
									if (i_2_ == 338) {
										is[i_1_] = (byte) -116;
									} else if (i_2_ == 381) {
										is[i_1_] = (byte) -114;
									} else if ((i_2_ ^ 0xffffffff) == -8217) {
										is[i_1_] = (byte) -111;
									} else if (i_2_ == 8217) {
										is[i_1_] = (byte) -110;
									} else if (i_2_ != 8220) {
										if (i_2_ == 8221) {
											is[i_1_] = (byte) -108;
										} else if ((i_2_ ^ 0xffffffff) == -8227) {
											is[i_1_] = (byte) -107;
										} else if ((i_2_ ^ 0xffffffff) != -8212) {
											if (i_2_ == 8212) {
												is[i_1_] = (byte) -105;
											} else if ((i_2_ ^ 0xffffffff) != -733) {
												if (i_2_ != 8482) {
													if (i_2_ == 353) {
														is[i_1_] = (byte) -102;
													} else if (i_2_ != 8250) {
														if ((i_2_ ^ 0xffffffff) == -340) {
															is[i_1_] = (byte) -100;
														} else if (i_2_ != 382) {
															if (i_2_ == 376) {
																is[i_1_] = (byte) -97;
															} else {
																is[i_1_] = (byte) 63;
															}
														} else {
															is[i_1_] = (byte) -98;
														}
													} else {
														is[i_1_] = (byte) -101;
													}
												} else {
													is[i_1_] = (byte) -103;
												}
											} else {
												is[i_1_] = (byte) -104;
											}
										} else {
											is[i_1_] = (byte) -106;
										}
									} else {
										is[i_1_] = (byte) -109;
									}
								} else {
									is[i_1_] = (byte) -117;
								}
							} else {
								is[i_1_] = (byte) -121;
							}
						} else {
							is[i_1_] = (byte) -122;
						}
					} else {
						is[i_1_] = (byte) -123;
					}
				} else {
					is[i_1_] = (byte) -128;
				}
			} else {
				is[i_1_] = (byte) i_2_;
			}
		}
		return is;
	}

	public static String method2122(byte[] is, int i, int i_11_) {
		char[] cs = new char[i_11_];
		int i_13_ = 0;
		for (int i_14_ = 0; i_14_ < i_11_; i_14_++) {
			int i_15_ = is[i + i_14_] & 0xff;
			if (i_15_ != 0) {
				if (i_15_ >= 128 && i_15_ < 160) {
					int i_16_ = aCharArray6385[i_15_ - 128];
					if (0 == i_16_) {
						i_16_ = 63;
					}
					i_15_ = i_16_;
				}
				cs[i_13_++] = (char) i_15_;
			}
		}
		return new String(cs, 0, i_13_);
	}

	/**
	 * A method used when decoding the script configurations.
	 * @param i The value.
	 * @return The character.
	 */
	public static char method6566(byte i) {
		int index = i & 0xff;
		if (index == 0) {
			throw new IllegalArgumentException("Non cp1252 character 0x" + Integer.toString(i, 16) + " provided");
		}
		if (index >= 128 && index < 160) {
			int character = aCharArray6385[index - 128];
			if (character == 0) {
				character = 63;
			}
			index = character;
		}
		return (char) index;
	}

	public static int method6567(char character) {
		for (int i = 0; i < aCharArray6385.length; i++) {
			if (character == aCharArray6385[i]) {
				return i + Byte.MAX_VALUE + 1;
			}
		}
		return character;
	}

	/**
	 * Copies all bytes from the input stream to the output stream. Does not close or flush either
	 * stream.
	 *
	 * @param from the input stream to read from
	 * @param to the output stream to write to
	 * @return the number of bytes copied
	 */
	public static long copy(InputStream from, OutputStream to) throws IOException {
		byte[] buf = new byte[0x1000];
		long total = 0;
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}

	public static int to317Hash(String name) {
		int hash = 0;
		name = name.toUpperCase();
		for (int i = 0; i < name.length(); i++) {
			hash = (hash * 61 + name.charAt(i)) - 32;
		}
		return hash;
	}

}