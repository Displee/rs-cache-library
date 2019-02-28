package org.displee.cache.index.archive;

import org.displee.cache.Container;
import org.displee.cache.index.Index;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.Compression;

/**
 * A class representing the information of an {@link Index}.
 * @author Displee
 */
public class ArchiveSector implements Container {

	/**
	 * The archive type.
	 */
	private int type;

	/**
	 * The size of this archive.
	 */
	private final int size;

	/**
	 * The position of this archive.
	 */
	private int position;

	/**
	 * The id of this archive.
	 */
	private int id;

	/**
	 * The chunk.
	 */
	private int chunk;

	/**
	 * The next position to read from.
	 */
	private int nextPosition;

	/**
	 * The index of this archive.
	 */
	private int index;

	/**
	 * The data of this archive.
	 */
	private byte[] data;

	/**
	 * The keys of this archive.
	 */
	private int[] keys;

	/**
	 * The compression type.
	 */
	private Compression.CompressionType compression;

	/**
	 * Constructs a new {@code Archive} {@code Object}.
	 * @param size The size.
	 * @param position The position.
	 */
	public ArchiveSector(int type, int size, int position) {
		this(type, size, position, -1, -1);
	}

	/**
	 * Constructs a new {@code ArchiveSector} {@code Object}.
	 * @param type The type.
	 * @param size The size.
	 * @param position The position.
	 * @param id The id.
	 * @param index The index.
	 */
	public ArchiveSector(int type, int size, int position, int id, int index) {
		this.type = type;
		this.size = size;
		this.position = position;
		this.id = id;
		this.index = index;
		this.data = new byte[size];
	}

	@Override
	public boolean read(InputStream inputStream) {
		if (type == 0) {
			id = inputStream.readUnsignedShort();
		} else if (type == 1) {
			id = inputStream.readInt();
		}
		chunk = inputStream.readUnsignedShort();
		nextPosition = inputStream.read24BitInt();
		index = inputStream.readUnsignedByte();
		return true;
	}

	@Override
	public byte[] write(OutputStream outputStream) {
		if (type == 0) {
			outputStream.writeShort(id);
		} else if (type == 1) {
			outputStream.writeInt(id);
		}
		outputStream.writeShort(chunk);
		outputStream.write24BitInt(position);
		outputStream.writeByte(index);
		return outputStream.flip();
	}

	/**
	 * Get the size of this sector.
	 * @return {@code size}
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Get the position of this sector.
	 * @return {@code id}
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Set a new position.
	 * @param position The new position to set.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Get the id of this archive.
	 * @return {@code id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the chunk of this archive.
	 * @return {@code chunk}
	 */
	public int getChunk() {
		return chunk;
	}

	/**
	 * Set a new value for the chunk.
	 * @param chunk The new chunk value to set.
	 */
	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	/**
	 * Get the next position.
	 * @return {@code nextPosition}
	 */
	public int getNextPosition() {
		return nextPosition;
	}

	/**
	 * Get the index of this archive.
	 * @return {@code index}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get the data of this archive.
	 * @return {@code data}
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Set a new array of data for this archive sector.
	 * @param data The new data to set.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Set the argued data for this archive.
	 * @param index The index.
	 * @param value The value.
	 */
	public void setData(int index, byte value) {
		data[index] = value;
	}

	/**
	 * Get the keys of this archive.
	 * @return The {@code keys}.
	 */
	public final int[] getKeys() {
		return keys;
	}

	/**
	 * Set the keys of this archive.
	 * @param keys The new {@code keys}.
	 */
	public final void setKeys(int[] keys) {
		this.keys = keys;
	}

	/**
	 * Get the type.
	 * @return {@code type}
	 */
	public int getType() {
		return type;
	}

	/**
	 * Set a new type.
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Set a new compression type.
	 * @param compression The new compression type to set.
     */
	public void setCompression(Compression.CompressionType compression) {
		this.compression = compression;
	}

	/**
	 * Get the compression type.
	 * @return {@code compression}
	 */
	public Compression.CompressionType getCompression() {
		return compression;
	}

}