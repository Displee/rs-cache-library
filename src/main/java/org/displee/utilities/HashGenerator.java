package org.displee.utilities;

import java.util.zip.CRC32;

/**
 * A class used to generate crc32 and whirlpool hashes.
 * @author Displee
 */
public class HashGenerator {

	/**
	 * The CRC instance.
	 */
	public static final CRC32 CRC = new CRC32();

	/**
	 * Get the CRC hash from the argued array of data.
	 * @param data The data.
	 * @return The CRC hash.
	 */
	public static int getCRCHash(byte[] data) {
		return getCRCHash(data, 0, data.length);
	}

	/**
	 * Get the CRC hash from the argued array of data.
	 * @param data The data.
	 * @param offset The start offset.
	 * @param length The length.
	 * @return The CRC hash.
	 */
	public static int getCRCHash(byte[] data, int offset, int length) {
		CRC.update(data, offset, length);
		final int hash = (int)CRC.getValue();
		CRC.reset();
		return hash;
	}

}