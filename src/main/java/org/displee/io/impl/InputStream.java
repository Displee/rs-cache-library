package org.displee.io.impl;

import org.displee.io.Stream;
import org.displee.utilities.Miscellaneous;

/**
 * A class representing an input stream.
 * @author Apache Ah64
 */
public class InputStream extends Stream {

	/**
	 * Construct a new {@code InputStream} {@code Object}.
	 */
	public InputStream() {
		super();
	}

	/**
	 * Construct a new {@code InputStream} {@code Object}.
	 * @param capacity The capacity.
	 */
	public InputStream(int capacity) {
		super((short) capacity);
	}

	/**
	 * Construct a new {@code InputStream} {@code Object}.
	 * @param bytes The bytes.
	 */
	public InputStream(byte[] bytes) {
		super(bytes);
	}

	/**
	 * Read the packet opcode.
	 * @return The opcode.
	 */
	public int readPacket() {
		return readByte() & 0xFF;
	}

	/**
	 * Read a long.
	 * @return The long.
	 */
	public long readLong() {
		long l = readInt() & 0xffffffffL;
		long l1 = readInt() & 0xffffffffL;
		return (l << 32) + l1;
	}

	/**
	 * Read a version 1 integer.
	 * @return The integer.
	 */
	public int readIntV1() {
		return ((readByte() & 0xFF) << 8) + (readByte() & 0xFF) + ((readByte() & 0xFF) << 24) + ((readByte() & 0xFF) << 16);
	}

	/**
	 * Read a version 2 integer.
	 * @return The integer.
	 */
	public int readIntV2() {
		return ((readByte() & 0xFF) << 16) + ((readByte() & 0xFF) << 24) + (readByte() & 0xFF) + ((readByte() & 0xFF) << 8);
	}

	/**
	 * Read a LE integer.
	 * @return The integer.
	 */
	public int readIntLE() {
		return (readByte() & 0xFF) + ((readByte() & 0xFF) << 8) + ((readByte() & 0xFF) << 16) + ((readByte() & 0xFF) << 24);
	}

	/**
	 * Read a 24 bit integer.
	 * @return The integer.
	 */
	public int read24BitInt() {
		return ((readByte() & 0xFF) << 16) + ((readByte() & 0xFF) << 8) + ((readByte() & 0xFF));
	}

	/**
	 * Read a smart value.
	 * @return The smart value.
	 */
	public int readSmart2() {
		int i = 0;
		int i_33_ = readUnsignedSmart();
		while (i_33_ == Short.MAX_VALUE) {
			i_33_ = readUnsignedSmart();
			i += Short.MAX_VALUE;
		}
		i += i_33_;
		return i;
	}

	/**
	 * Read an unsigned smart.
	 * @return The smart value.
	 */
	public int readSmart() {
		final int i = buffer[offset] & 0xFF;
		if (i < 128) {
			return readUnsignedByte() - 64;
		}
		return readUnsignedShort() - 49152;
	}

	/**
	 * Reads a smart value from the buffer (supports -1).
	 * @return the read smart value.
	 */
	public int readSmartNS() {
		return readUnsignedSmart() - 1;
	}

	/**
	 * Read a signed smart.
	 * @return The smart value.
	 */
	public int readUnsignedSmart() {
		final int i = buffer[offset] & 0xFF;
		if (i < 128) {
			return readUnsignedByte();
		}
		return readUnsignedShort() - 32768;
	}

	public int readBigSmart(boolean old) {
		if (old) {
			return readUnsignedShort();
		} else {
			return readBigSmart();
		}
	}

	public int readBigSmart() {
		if (buffer[offset] < 0) {
			return readInt() & 0x7fffffff;
		}
		int value = readUnsignedShort();
		if (value == Short.MAX_VALUE) {
			return -1;
		}
		return value;
	}

	public float readIntAsFloat() {
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Read a short.
	 * @return The short.
	 */
	public int readShort() {
		int s = ((readByte() & 0xFF) << 8) + (readByte() & 0xFF);
		if (s > Short.MAX_VALUE) {
			s -= 0x10000;
		}
		return s;
	}

	/**
	 * Read a short unsigned.
	 * @return The unsigned short.
	 */
	public int readUnsignedShort() {
		/*int i = readUnsignedByte();
		return readUnsignedByte() + (i << 8);*/
		return readShort() & 0xFFFF;
	}

	/**
	 * Read a short LE.
	 * @return The short.
	 */
	public int readShortLE() {
		int s = (readByte() & 0xFF) + ((readByte() & 0xFF) << 8);
		if (s > Short.MAX_VALUE) {
			s -= 0x10000;
		}
		return s;
	}

	/**
	 * Read a short with a size of 128 added.
	 * @return The short.
	 */
	public int readShort128() {
		int s = ((readByte() & 0xFF) << 8) + (readByte() - 128 & 0xFF);
		if (s > Short.MAX_VALUE) {
			s -= 0x10000;
		}
		return s;
	}

	/**
	 * Read a short LE with a size of 128 added.
	 * @return The short.
	 */
	public int readShortLE128() {
		int s = (readByte() - 128 & 0xFF) + ((readByte() & 0xFF) << 8);
		if (s > Short.MAX_VALUE) {
			s -= 0x10000;
		}
		return s;
	}

	/**
	 * Read a short LE with a base of 128 and a byte added.
	 * @return The short.
	 */
	public int read128ShortLE() {
		int s = (128 - readByte() & 0xFF) + ((readByte() & 0xFF) << 8);
		if (s > Short.MAX_VALUE) {
			s -= 0x10000;
		}
		return s;
	}

	/**
	 * Read a byte with a size of 128 added.
	 * @return The 128 byte.
	 */
	public int readByte128() {
		return (byte) (readByte() - 128);
	}

	/**
	 * Read a byte with a size of -128 and the actual byte added.
	 * @return The 128 byte.
	 */
	public int read128Byte() {
		return (byte) (128 - readByte());
	}

	/**
	 * Read a negative byte.
	 * @return The negative byte.
	 */
	public int readNegativeByte() {
		return (byte) -readByte();
	}

	public String readMagicString() {
		if (buffer[offset] == 0) {
			++offset;
			return null;
		}
		return readString();
	}

	/**
	 * Read a Jagex string.
	 * @return The string.
	 */
	public String readJagString() {
		readByte();
		return readString();
	}

	/**
	 * Read a string.
	 * @return The string.
	 */
	public String readString() {
		int i_22_ = offset;
		while (buffer[offset++] != 0) {
			/* empty */
		}
		int i_23_ = offset - i_22_ - 1;
		if (i_23_ == 0) {
			return "";
		}
		return Miscellaneous.method2122(buffer, i_22_, i_23_);
	}

	public String readString317() {
		int start = offset;
		while (buffer[offset++] != 10) {

		}
		return new String(buffer, start, offset - start - 1);
	}

	public int smf_gvlength() {
		int i_20_ = readByte();
		int i_21_ = 0;
		for (; i_20_ < 0; i_20_ = readByte()) {
			i_21_ = (i_21_ | i_20_ & 0x7f) << 7;
		}
		return i_21_ | i_20_;
	}

	public boolean readBoolean() {
		return readUnsignedByte() == 1;
	}

	/**
	 * Read the bytes.
	 * @param length The amount to read.
	 * @return The bytes.
	 */
	public byte[] readBytes(int length) {
		return readBytes(0, length);
	}

	/**
	 * Read the bytes.
	 * @param offset The offset to start from.
	 * @param length The amount to read.
	 * @return The bytes.
	 */
	public byte[] readBytes(int offset, int length) {
		byte[] bytes = new byte[length];
		readBytes(bytes, offset, bytes.length);
		return bytes;
	}

	/**
	 * Read the bytes.
	 * @param bytes The bytes.
	 */
	public void readBytes(byte[] bytes) {
		readBytes(bytes, 0, bytes.length);
	}

	/**
	 * Read the bytes.
	 * @param bytes The bytes.
	 * @param offset The offset.
	 * @param length The length.
	 */
	public void readBytes(byte[] bytes, int offset, int length) {
		for (int k = offset; k < length + offset; k++) {
			bytes[k] = (byte) readByte();
		}
	}

	public void readBytesSmart(byte[] bytes, int offset, int length, boolean resetOffset) {
		int i = 0;
		for (int k = offset; k < length + offset; k++) {
			bytes[i++] = (byte) readByte();
		}
		if (resetOffset) {
			setOffset(offset);
		}
	}

	/**
	 * Read multiple bytes and supply them to a safe-byte-array.
	 * @param bytes The safe-byte-array.
	 */
	public void readMultipleBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) readByte();
		}
	}

	/**
	 * Supply bytes to the input stream.
	 * @param bytes The bytes.
	 */
	public void supplyBytes(byte[] bytes) {
		supplyBytes(bytes, 0, bytes.length);
	}

	/**
	 * Supply bytes to the input stream.
	 * @param bytes The bytes.
	 * @param offset The offset.
	 * @param length The length.
	 */
	public void supplyBytes(byte[] bytes, int offset, int length) {
		for (; offset < length; offset++) {
			this.buffer[offset] = bytes[offset];
		}
	}

	public int[][] read2DIntArray() {
		int[][] array = new int[readUnsignedShort()][];
		for(int i = 0; i < array.length; i++) {
			array[i] = new int[readUnsignedShort()];
			for(int i2 = 0; i2 < array[i].length; i2++) {
				array[i][i2] = readShort();
			}
		}
		return array;
	}

	public byte[][] read2DByteArray() {
		byte[][] array = new byte[readUnsignedShort()][];
		for(int i = 0; i < array.length; i++) {
			array[i] = new byte[readUnsignedShort()];
			readBytes(array[i]);
		}
		return array;
	}

	public boolean[][] read2DBooleanArray() {
		boolean[][] array = new boolean[readUnsignedShort()][];
		for(int i = 0; i < array.length; i++) {
			array[i] = new boolean[readUnsignedShort()];
			for(int i2 = 0; i2 < array[i].length; i2++) {
				array[i][i2] = readByte() == 1;
			}
		}
		return array;
	}

	/**
	 * Skip bytes to be read.
	 * @param length
	 */
	public void skip(int length) {
		offset += length;
	}

	/**
	 * Flip the bytes in this inputstream.
	 * @return The data.
	 */
	public byte[] flip() {
		byte[] data = new byte[offset];
		readBytes(data, 0, offset);
		return data;
	}

	/**
	 * Flip the bytes in this inputstream.
	 * @param length The length to read.
	 * @return The data.
	 */
	public byte[] flip(int length) {
		byte[] data = new byte[length];
		readBytes(data, 0, length);
		return data;
	}

}