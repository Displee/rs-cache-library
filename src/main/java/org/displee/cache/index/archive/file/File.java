package org.displee.cache.index.archive.file;

import org.displee.cache.index.archive.Archive;

/**
 * A class that represents the a single file inside an {@link Archive}.
 * @author Displee
 */
public class File {

	/**
	 * The id of this file.
	 */
	private int id;

	/**
	 * The unformatted name of this file.
	 */
	private int name;

	/**
	 * The data of this file.
	 */
	private byte[] data;

	/**
	 * Constructs a new {@code File} {@code Object}.
	 */
	public File(int id) {
		this.id = id;
	}

	/**
	 * Constructs a new {@code FIle} {@code Object}.
	 * @param id The file id.
	 * @param data The file data.
	 */
	public File(int id, byte[] data) {
		this(id, data, -1);
	}


	/**
	 * Consturcts a new {@code File} {@code Object}.
	 * @param id The file id.
	 * @param data The file data.
	 * @param name The file name.
	 */
	public File(int id, byte[] data, int name) {
		this.id = id;
		this.data = data;
		this.name = name;
	}

	/**
	 * Copy this file.
	 * @return The new file.
	 */
	public File copy() {
		return new File(id, data.clone(), name);
	}

	/**
	 * Get the id of this file.
	 * @return {@code id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set a new id for this file.
	 * @param id The new id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set a new unformatted name for this file.
	 * @param name The new name to set.
	 */
	public void setName(int name) {
		this.name = name;
	}

	/**
	 * Get the unformatted name.
	 * @return {@code name}
	 */
	public int getName() {
		return name;
	}

	/**
	 * Set a new array of data for this file.
	 * @param data The array of data to set.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Get the data of this file.
	 * @return {@code data}
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Get the info about this file.
	 * @return The info.
	 */
	public String getInfo() {
		return "File[id=" + id + ", name=" + name + "]";
	}
	
	@Override
	public String toString() {
		return "File " + id;
	}

}