package org.displee.utilities;

/**
 * A class that holds the constants of this cache library.
 * @author Displee
 */
public class Constants {

	/**
	 * The size of an index in bytes.
	 */
	public static final int INDEX_SIZE = 6;

	/**
	 * The size of the header within an archive sector in bytes.
	 */
	public static final int ARCHIVE_HEADER_SIZE = 8;

	/**
	 * The size of the data within an archive sector in bytes.
	 */
	public static final int ARCHIVE_DATA_SIZE = 512;

	/**
	 * The total size of an archive sector in bytes.
	 */
	public static final int ARCHIVE_SIZE = ARCHIVE_HEADER_SIZE + ARCHIVE_DATA_SIZE;

	/**
	 * The size of a whirlpool hash in bytes.
	 */
	public static final int WHIRLPOOL_SIZE = 64;

}
