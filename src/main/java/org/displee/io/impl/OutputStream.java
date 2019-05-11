package org.displee.io.impl;

import org.displee.io.Stream;
import org.displee.utilities.Miscellaneous;

/**
 * A class representing an output stream.
 * @author Apache Ah64
 */
public class OutputStream extends Stream {

	/**
	 * The bit mask.
	 */
	private static final int[] BIT_MASK = new int[32];

	/**
	 * The opcode start.
	 */
	private int opcodeStart;

	/**
	 * Construct a new {@code OutputStream} {@code Object}.
	 */
	public OutputStream() {
		super();
	}

	/**
	 * Construct a new {@code OutputStream} {@code Object}.
	 * @param capacity The capacity.
	 */
	public OutputStream(int capacity) {
		super(capacity);
	}

	/**
	 * Construct a new {@code OutputStream} {@code Object}.
	 * @param bytes The bytes.
	 */
	public OutputStream(byte[] bytes) {
		super(bytes);
	}

	/**
	 * Write a packet variable byte.
	 * @param id The id.
	 */
	@Deprecated
	public void writePacketVarByte(int id) {
		writeUnsignedSmart(id);
		writeByte(0);
		opcodeStart = getOffset() - 1;
	}

	/**
	 * Write a packet.
	 * @param id
	 */
	public void writePacket(int id) {
		writeByte(id);
	}

	/**
	 * Write a packet variable short.
	 * @param id The id.
	 */
	@Deprecated
	public void writePacketVarShort(int id) {
		writeUnsignedSmart(id);
		writeShort(0);
		opcodeStart = getOffset() - 2;
	}

	/**
	 * End the packet variable byte.
	 */
	@Deprecated
	public void endPacketVarByte() {
		writeByte((byte) (getOffset() - (opcodeStart + 2) + 1), opcodeStart);
	}

	/**
	 * End the packet variable short.
	 */
	@Deprecated
	public void endPacketVarShort() {
		int size = getOffset() - (opcodeStart + 2);
		writeByte((byte) (size >> 8), opcodeStart++);
		writeByte((byte) size, opcodeStart);
	}

	public void writeBoolean(boolean value) {
		writeByte(value ? 1 : 0);
	}

	/**
	 * Write a smart.
	 * @param i The integer.
	 */
	public void writeUnsignedSmart(int i) {
		if (i < 128) {
			writeByte((byte) i);
			return;
		}
		writeShort(i + 32768);
	}

	public void writeSmart2(int i) {
		while (i >= Short.MAX_VALUE) {
			writeUnsignedSmart(Short.MAX_VALUE);
			i -= Short.MAX_VALUE;
		}
		writeUnsignedSmart(i);
	}

	/**
	 * Writes an unsigned smart value to the buffer.
	 * @param value The value to write.
	 */
	public void writeSmart(int value) {
		if (value < 64 && value >= -64) {
			writeByte(value + 64);
			return;
		}
		//if (value < 16384 && value >= -16384) {
			writeShort(value + 49152);
		/*} else {
			System.err.println("Error psmart out of range: " + value);
		}*/
	}

	/**
	 * Write a big smart.
	 * @param i
	 */
	public void writeBigSmart(int i, boolean old) {
		if (old) {
			writeShort(i);
		} else {
			writeBigSmart(i);
		}
	}

	/**
	 * Write a big smart.
	 * @param i
	 */
	public void writeBigSmart(int i) {
		if (i >= Short.MAX_VALUE) {
			writeInt(i - Integer.MAX_VALUE - 1);
		} else {
			writeShort(i >= 0 ? i : Short.MAX_VALUE);
		}
	}

	/**
	 * Write a long.
	 * @param l The long.
	 */
	public void writeLong(long l) {
		writeByte((int) (l >> 56));
		writeByte((int) (l >> 48));
		writeByte((int) (l >> 40));
		writeByte((int) (l >> 32));
		writeByte((int) (l >> 24));
		writeByte((int) (l >> 16));
		writeByte((int) (l >> 8));
		writeByte((int) l);
	}

	/**
	 * Write a big retarded value.
	 * @param value The value.
	 */
	public void write5ByteInteger(long value) {
		writeByte((int) (value >> 32));
		writeInt((int) value);
	}

	/**
	 * Write a version 1 integer.
	 * @param i The integer
	 */
	public void writeIntV1(int i) {
		writeByte(i >> 8);
		writeByte(i);
		writeByte(i >> 24);
		writeByte(i >> 16);
	}

	/**
	 * Write a version 2 integer.
	 * @param i The integer
	 */
	public void writeIntV2(int i) {
		writeByte(i >> 16);
		writeByte(i >> 24);
		writeByte(i);
		writeByte(i >> 8);
	}

	/**
	 * Write a integer LE.
	 * @param i The integer
	 */
	public void writeIntLE(int i) {
		writeByte(i);
		writeByte(i >> 8);
		writeByte(i >> 16);
		writeByte(i >> 24);
	}

	/**
	 * Write a 24 bit integer.
	 * @param i The integer.
	 */
	public void write24BitInt(int i) {
		writeByte(i >> 16);
		writeByte(i >> 8);
		writeByte(i);
	}

	/**
	 * Write a second version 24 bit integer.
	 * @param i The integer.
	 */
	public void write24BitIntV2(int i) {
		writeByte(i >> 16);
		writeByte(i);
		writeByte(i >> 8);
	}

	/**
	 * Write a short.
	 * @param s The short.
	 */
	public void writeShort(int s) {
		writeByte(s >> 8);
		writeByte(s);
	}

	/**
	 * Write a short with a size of 128 added.
	 * @param s The short.
	 */
	public void writeShort128(int s) {
		writeByte(s >> 8);
		writeByte(s + 128);
	}

	/**
	 * Write a short LE.
	 * @param s The short.
	 */
	public void writeShortLE(int s) {
		writeByte(s);
		writeByte(s >> 8);
	}

	/**
	 * Write a short LE with a size of 128 added.
	 * @param s The short.
	 */
	public void writeShortLE128(int s) {
		writeByte(s + 128);
		writeByte(s >> 8);
	}

	/**
	 * Write a byte with a size of 128 added.
	 * @param b The byte.
	 */
	public void writeByte128(int b) {
		writeByte((byte) b + 128);
	}

	/**
	 * Write a byte with a size of -128 and the actual byte added.
	 * @param b The byte.
	 */
	public void write128Byte(int b) {
		writeByte((byte) 128 - b);
	}

	/**
	 * Write a byte with a size of -128 plus actual byte.
	 * @param b The byte.
	 */
	public void writeNegative128Byte(int b) {
		writeByte((byte) -128 + b);
	}

	/**
	 * Write a negative byte.
	 * @param b The byte.
	 */
	public void writeNegativeByte(int b) {
		writeByte((byte) -b);
	}

	public void method13177(int i) {
		if ((i & ~0x7f) != 0) {
			if ((i & ~0x3fff) != 0) {
				if ((i & ~0x1fffff) != 0) {
					if ((i & ~0xfffffff) != 0) {
						writeByte(i >>> 28 | 0x80);
					}
					writeByte(i >>> 21 | 0x80);
				}
				writeByte(i >>> 14 | 0x80);
			}
			writeByte(i >>> 7 | 0x80);//(0x4028 | arg1) >>> 7
		}
		writeByte(i & 0x7f);
	}

	public void method13180(int i) {
		buffer[offset - i - 4] = (byte) (i >> 24);
		buffer[offset - i - 3] = (byte) (i >> 16);
		buffer[offset - i - 2] = (byte) (i >> 8);
		buffer[offset - i - 1] = (byte) i;
	}

	/**
	 * Initialize bit access.
	 */
	public void initBitAccess() {
		bitPosition = offset * 8;
	}

	/**
	 * Write bits.
	 * @param numBits The bit numbers.
	 * @param value   The values.
	 */
	public void writeBits(int numBits, int value) {
		int bytePos = bitPosition >> 3;
		int bitOffset = 8 - (bitPosition & 7);
		bitPosition += numBits;
		for (; numBits > bitOffset; bitOffset = 8) {
			expend(bytePos);
			buffer[bytePos] &= ~BIT_MASK[bitOffset];
			buffer[bytePos++] |= value >> numBits - bitOffset & BIT_MASK[bitOffset];
			numBits -= bitOffset;
		}
		expend(bytePos);
		if (numBits == bitOffset) {
			buffer[bytePos] &= ~BIT_MASK[bitOffset];
			buffer[bytePos] |= value & BIT_MASK[bitOffset];
		} else {
			buffer[bytePos] &= ~(BIT_MASK[numBits] << bitOffset - numBits);
			buffer[bytePos] |= (value & BIT_MASK[numBits]) << bitOffset - numBits;
		}
	}

	/**
	 * Finish the bit access.
	 */
	public void finishBitAccess() {
		offset = (bitPosition + 7) / 8;
	}

	/**
	 * Get the bit position.
	 * @param i The index.
	 * @return The position.
	 */
	public int getBitPos(int i) {
		return 8 * i - bitPosition;
	}

	public void writeMagicString(String string) {
		if (string == null) {
			writeByte(0);
			return;
		}
		writeString(string);
	}

	/**
	 * Write a Jagex string.
	 * @param string The string.
	 */
	public void writeJagString(String string) {
		writeByte((byte) 0);
		writeString(string);
	}

	/**
	 * Write a string.
	 * @param string The string.
	 */
	public void writeString(String string) {
		writeBytes(Miscellaneous.getFormattedMessage(string));
		writeByte(0);
	}

	public void writeString317(String string) {
		writeBytes(string.getBytes());
		writeByte(10);
	}

	/**
	 * Write bytes.
	 * @param bytes The bytes.
	 */
	public void writeBytes(byte[] bytes) {
		writeBytes(bytes, 0, bytes.length);
	}

	/**
	 * Write bytes.
	 * @param bytes  The bytes.
	 * @param offset The offset.
	 * @param length The length.
	 */
	public void writeBytes(byte[] bytes, int offset, int length) {
		expend(this.offset + length - offset);
		System.arraycopy(bytes, offset, this.buffer, this.offset, length);
		this.offset += (length - offset);
	}

	/**
	 * Write reversed bytes.
	 * @param bytes  The bytes.
	 * @param offset The offset.
	 * @param length The length.
	 */
	public void writeReversedBytes(byte[] bytes, int offset, int length) {
		expend(this.offset + length - offset);
		final byte[] copy = bytes.clone();
		for (int i = 0; i < copy.length; i++) {
			bytes[copy.length - 1 - i] = bytes[i];
		}
		System.arraycopy(bytes, offset, this.buffer, this.offset, length);
		this.offset += (length - offset);
	}

	public void write2DIntArray(int[][] array) {
		writeShort(array.length);
		for (int[] i : array) {
			writeShort(i.length);
			for (int i2 : i) {
				writeShort(i2);
			}
		}
	}

	public void write2DByteArray(byte[][] array) {
		writeShort(array.length);
		for (byte[] i : array) {
			writeShort(i.length);
			writeBytes(i);
		}
	}

	public void write2DBooleanArray(boolean[][] array) {
		writeShort(array.length);
		for (boolean[] i : array) {
			writeShort(i.length);
			for (boolean i2 : i) {
				writeByte(i2 ? 1 : 0);
			}
		}
	}

	public void writeArray(short[] array) {
		writeShort(array == null ? 0 : array.length);
		if (array != null) {
			for (short i : array) {
				writeShort(i);
			}
		}
	}

	public void writeArray(int[] array) {
		writeShort(array == null ? 0 : array.length);
		if (array != null) {
			for (int i : array) {
				writeInt(i);
			}
		}
	}

	static {
		for (int i = 0; i < 32; i++) {
			BIT_MASK[i] = (1 << i) - 1;
		}
	}

	/**
	 * Flip the data in this outputstream.
	 * @return The data.
	 */
	public byte[] flip() {
		byte[] data = new byte[offset];
		clone(data, offset = 0, data.length);
		return data;
	}

}
