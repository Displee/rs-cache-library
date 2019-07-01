package org.displee.io;

/**
 * A class representing the base for both I/O implementations.
 *
 * @author Displee
 */
public abstract class Stream {

	/**
	 * The default capacity.
	 */
	public static final int DEFAULT_CAPACITY = 16;

	/**
	 * The bytes.
	 */
	protected byte[] buffer;

	/**
	 * The offset.
	 */
	protected int offset;

	/**
	 * The bit position.
	 */
	protected int bitPosition;

	/**
	 * Construct a new {@code Stream} {@code Object}.
	 */
	public Stream() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Construct a new {@code Stream} {@code Object}.
	 *
	 * @param capacity The capacity.
	 */
	public Stream(int capacity) {
		try {
			if (capacity < 0 || capacity >= Integer.MAX_VALUE) {
				throw new RuntimeException("Illegal capacity");
			}
			buffer = new byte[capacity];
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct a new {@code Stream} {@code Object}.
	 *
	 * @param bytes The bytes.
	 */
	public Stream(byte[] bytes) {
		this.buffer = bytes;
	}

	/**
	 * Write an integer.
	 *
	 * @param i The integer.
	 */
	public void writeInt(int i) {
		writeByte(i >> 24);
		writeByte(i >> 16);
		writeByte(i >> 8);
		writeByte(i);
	}

	public void writeReversedInt(int i) {
		writeByte(i);
		writeByte(i >> 8);
		writeByte(i >> 16);
		writeByte(i >> 24);
	}

	public void writeFloatAsInt(float i) {
		writeInt(Float.floatToRawIntBits(i));
	}

	public void writeFloatAsReversedInt(float i) {
		writeReversedInt(Float.floatToRawIntBits(i));
	}

	/**
	 * Read an integer.
	 *
	 * @return The integer.
	 */
	public int readInt() {
		return (readUnsignedByte() << 24) + (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + readUnsignedByte();
	}

	/**
	 * Read a byte.
	 *
	 * @return The byte.
	 */
	public int readByte() {
		return getRemaining() > 0 ? buffer[offset++] : 0;
	}

	/**
	 * Read an unsigned byte.
	 * @return The unsigned byte.
	 */
	public int readUnsignedByte() {
		return readByte() & 0xFF;
	}

	/**
	 * Write a byte.
	 * @param b The byte.
	 */
	public void writeByte(int b) {
		writeByte((byte) b);
	}

	/**
	 * Write a byte.
	 * @param b The byte.
	 */
	public void writeByte(byte b) {
		writeByte(b, offset++);
	}

	/**
	 * Write a byte.
	 * @param b     The byte.
	 * @param index The index.
	 */
	public void writeByte(byte b, int index) {
		expend(index);
		buffer[index] = b;
	}

	/**
	 * Write a boolean.
	 *
	 * @param bool The condition.
	 */
	public void writeBoolean(boolean bool) {
		writeByte(bool ? 1 : 0);
	}

	/**
	 * Expend the capacity.
	 */
	public void expend(int offset) {
		if (offset >= buffer.length) {
			final byte[] newBytes = new byte[offset + DEFAULT_CAPACITY];
			System.arraycopy(buffer, 0, newBytes, 0, buffer.length);
			buffer = newBytes;
		}
	}

	/**
	 * Literally clone the entire byte array.
	 *
	 * @param bytes The new bytes array.
	 */
	public void clone(byte[] bytes) {
		bytes = this.buffer;
	}

	/**
	 * Clone bytes.
	 *
	 * @param bytes  The bytes.
	 * @param offset The offset.
	 * @param length The length.
	 */
	public void clone(byte[] bytes, int offset, int length) {
		if (bytes.length < length) {
			bytes = new byte[length];
		}
		for (; offset < length; offset++) {
			bytes[offset] = this.buffer[offset];
		}
	}

	public void decodeXTEA(int keys[], int start, int end) {
		int l = offset;
		offset = start;
		int i1 = (end - start) / 8;
		for (int j1 = 0; j1 < i1; j1++) {
			int k1 = readInt();
			int l1 = readInt();
			int sum = 0xc6ef3720;
			int delta = 0x9e3779b9;
			for (int k2 = 32; k2-- > 0;) {
				l1 -= keys[(sum & 0x1c84) >>> 11] + sum ^ (k1 >>> 5 ^ k1 << 4) + k1;
				sum -= delta;
				k1 -= (l1 >>> 5 ^ l1 << 4) + l1 ^ keys[sum & 3] + sum;
			}
			offset -= 8;
			writeInt(k1);
			writeInt(l1);
		}
		offset = l;
	}

	public final void encodeXTEA(int keys[], int start, int end) {
		int o = offset;
		int j = (end - start) / 8;
		offset = start;
		for (int k = 0; k < j; k++) {
			int l = readInt();
			int i1 = readInt();
			int sum = 0;
			int delta = 0x9e3779b9;
			for (int l1 = 32; l1-- > 0;) {
				l += sum + keys[3 & sum] ^ i1 + (i1 >>> 5 ^ i1 << 4);
				sum += delta;
				i1 += l + (l >>> 5 ^ l << 4) ^ keys[(0x1eec & sum) >>> 11] + sum;
			}
			offset -= 8;
			writeInt(l);
			writeInt(i1);
		}
		offset = o;
	}

	/**
	 * Get the remaining size available to be read.
	 *
	 * @return The remaining size.
	 */
	public int getRemaining() {
		return offset < buffer.length ? buffer.length - offset : 0;
	}

	/**
	 * Get the bytes serving as a buffer.
	 */
	public final byte[] getBytes() {
		return buffer;
	}

	/**
	 * Get the offset.
	 *
	 * @return The offset.
	 */
	public final int getOffset() {
		return offset;
	}

	/**
	 * Update the offset by added the argued amount.
	 *
	 * @param toIncrease The amount to increase the offset.
	 */
	public void updateOffset(int toIncrease) {
		this.offset += toIncrease;
	}

	/**
	 * Set a new offset.
	 *
	 * @param offset The offset.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
