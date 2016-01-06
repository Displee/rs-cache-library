package org.displee.cache.index.archive;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;

import org.displee.cache.Container;
import org.displee.cache.index.archive.file.File;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.Constants;

/**
 * A class that represents a single archive inside a single index.
 * @author Displee
 */
public class Archive implements Container {

	/**
	 * The id of this archive.
	 */
	private int id;

	/**
	 * The unformatted name of this archive.
	 */
	private int name;

	/**
	 * The whirlpool of this archive.
	 */
	private byte[] whirlpool = new byte[Constants.WHIRLPOOL_SIZE];

	/**
	 * The crc value of this archive.
	 */
	private int crc;

	/**
	 * The revision of this archive.
	 */
	private int revision;

	/**
	 * An array of file ids inside this archive.
	 */
	private int[] fileIds;

	/**
	 * The files of this archive.
	 */
	private File[] files;

	/**
	 * If this archive has been read.
	 */
	private boolean read;

	/**
	 * If this archive needs to be updated.
	 */
	private boolean needUpdate;

	/**
	 * If this archive is new.
	 */
	private boolean isNew;

	/**
	 * Consturcts a new {@code Archive} {@code Object}.
	 * @param id The id of this archive.
	 */
	public Archive(int id) {
		this.id = id;
	}

	/**
	 * Consturcts a new {@code Archive} {@code Object}.
	 * @param id The id of this archive.
	 * @param name The name of this archive.
	 */
	public Archive(int id, int name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean read(InputStream inputStream) {
		if (fileIds.length == 1) {
			files[0].setData(inputStream.getBytes());
		} else {
			int offsetPosition = inputStream.getBytes().length;
			int length = inputStream.getBytes()[--offsetPosition] & 0xFF;
			offsetPosition -= length * (fileIds.length * 4);
			int filesSize[] = new int[fileIds.length];
			inputStream.setOffset(offsetPosition);
			for (int i = 0; i < length; i++) {
				int offset = 0;
				for (int fileIndex = 0; fileIndex < fileIds.length; fileIndex++) {
					filesSize[fileIndex] += offset += inputStream.readInt();
				}
			}
			byte[][] filesData = new byte[fileIds.length][];
			for (int i = 0; i < fileIds.length; i++) {
				filesData[i] = new byte[filesSize[i]];
				filesSize[i] = 0;
			}
			inputStream.setOffset(offsetPosition);
			int offset = 0;
			for (int i = 0; i < length; i++) {
				int read = 0;
				for (int fileIndex = 0; fileIndex < fileIds.length; fileIndex++) {
					read += inputStream.readInt();
					System.arraycopy(inputStream.getBytes(), offset, filesData[fileIndex], filesSize[fileIndex], read);
					offset += read;
					filesSize[fileIndex] += read;
				}
			}
			for (int i = 0; i < fileIds.length; i++) {
				getFile(fileIds[i]).setData(filesData[i]);
			}
		}
		return read = true;
	}

	@Override
	public byte[] write(OutputStream outputStream) {
		if (files.length == 1) {
			return files[0].getData();
		} else {
			for (int i = 0; i < fileIds.length; i++) {
				final File file = getFile(fileIds[i]);
				if (file != null && file.getData() != null) {
					outputStream.writeBytes(file.getData());
				}
			}
			for (int i = 0; i < files.length; i++) {
				final File file = getFile(fileIds[i]);
				if (file != null && file.getData() != null) {
					outputStream.writeInt(file.getData().length - ((i == 0 || getFile(fileIds[i - 1]) == null || getFile(fileIds[i - 1]).getData() == null) ? 0 : getFile(fileIds[i - 1]).getData().length));
				}
			}
		}
		outputStream.writeByte(1);
		return outputStream.flip();
	}

	/**
	 * Add multiple files.
	 * @param files The array of the files to add.
	 */
	public void addFiles(File[] files) {
		for (File file : files) {
			addFile(file);
		}
	}

	/**
	 * Add a file instance to this archive.
	 * @param file The file instance.
	 */
	public File addFile(File file) {
		return addFile(file.getId(), file.getData(), file.getName());
	}

	/**
	 * Add a file to this archive.
	 * @param name The name.
	 * @return The file instance.
	 */
	public File addFile(String name) {
		return addFile(name);
	}

	/**
	 * Add a file to this archive.
	 * @param data The data of the file to add.
	 */
	public File addFile(byte[] data) {
		return addFile(null, data);
	}

	/**
	 * Add a new file to this archive.
	 * @param name The name of the file to add.
	 * @param data The data.
	 */
	public File addFile(String name, byte[] data) {
		final int fileId = getFileId(name);
		return addFile(fileId == -1 ? (getLastFile().getId() + 1) : fileId, data, name == null ? -1 : name.toLowerCase().hashCode());
	}

	/**
	 * Add a new file to this archive.
	 * @param id The id of the new file.
	 * @param data The data.
	 */
	public File addFile(int id, byte[] data) {
		return addFile(id, data, -1);
	}

	/**
	 * Add a new file to this archive.
	 * @param id The id of the new file.
	 * @param data The data.
	 * @param name The file name.
	 */
	public File addFile(int id, byte[] data, int name) {
		final File current = getFile(id);
		if (current != null) {
			boolean flag = false;
			if (!Arrays.equals(current.getData(), data)) {
				current.setData(data);
				flag = true;
			}
			if (name != -1 && current.getName() != name) {
				current.setName(name);
				flag = true;
			}
			if (flag) {
				flag();
			}
			return current;
		}
		fileIds = Arrays.copyOf(fileIds, fileIds.length + 1);
		fileIds[fileIds.length - 1] = id;
		files = Arrays.copyOf(files, files.length + 1);
		final File file = new File(id, data, name);
		files[files.length - 1] = file;
		flag();
		return file;

	}

	/**
	 * Remove a file from this archive.
	 * @param name The name.
	 */
	public void removeFile(String name) {
		removeFile(getFileId(name));
	}

	/**
	 * Remove a file from this archive.
	 * @param id The id of the file to delete.
	 */
	public void removeFile(int id) {
		try {
			boolean exists = false;
			for (final File file : files) {
				if (file.getId() == id) {
					exists = true;
				}
			}
			if (!exists) {
				throw new FileNotFoundException("The argued file id could not be removed as it is not stored in this archive.");
			}
			final int[] fileIds = new int[this.fileIds.length - 1];
			int offset = 0;
			for (int i = 0; i < this.fileIds.length; i++) {
				if (this.fileIds[i] != id) {
					fileIds[offset++] = this.fileIds[i];
				}
			}
			this.fileIds = fileIds;
			offset = 0;
			final File[] files = new File[this.files.length - 1];
			for (int i = 0; i < this.files.length; i++) {
				if (this.files[i].getId() != id) {
					files[offset++] = this.files[i];
				}
			}
			this.files = files;
			flag();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Flag this archive to be updated.
	 */
	public void flag() {
		sort();
		revision++;
		needUpdate = true;
	}

	/**
	 * Sort the holders of this archive.
	 */
	public void sort() {
		Arrays.sort(fileIds);
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				return file1.getId() > file2.getId() ? 0 : -1;
			}
		});
	}

	/**
	 * Reset the files of this archive
	 */
	public void reset() {
		files = new File[0];
		fileIds = new int[0];
	}

	/**
	 * Copy this archive.
	 * @return The new archive.
	 */
	public Archive copy() {
		final Archive archive = new Archive(id, name);
		archive.fileIds = fileIds;
		archive.files = files;
		archive.revision = revision;
		archive.crc = crc;
		archive.whirlpool = whirlpool;
		return archive;
	}

	/**
	 * Get the id of this archive.
	 * @return {@code id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set a new id for this archive.
	 * @param id The new id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set a new unformatted name for this archive.
	 * @param name The new name to set.
	 * @return
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
	 * Set a new whirlpool for this archive.
	 * @param whirlpool The new whirlpool to set.
	 */
	public void setWhirlpool(byte[] whirlpool) {
		this.whirlpool = whirlpool;
	}

	/**
	 * Get the whirlpool.
	 * @return {@code whirlpool}
	 */
	public byte[] getWhirlpool() {
		return whirlpool;
	}

	/**
	 * Set a new crc value for this archive.
	 * @param crc The new crc value to set.
	 */
	public void setCRC(int crc) {
		this.crc = crc;
	}

	/**
	 * Get the crc value.
	 * @return {@code crc}
	 */
	public int getCRC() {
		return crc;
	}

	/**
	 * Set a new revision value for this archive.
	 * @param revision The new revision value to set.
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * Get the revision of this archive.
	 * @return {@code revision}
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Set a new length for the file ids.
	 * @param length The new length to set.
	 */
	public void setFileIds(int length) {
		fileIds = new int[length];
	}

	/**
	 * Get the file ids.
	 * @return {@code fileIds}
	 */
	public int[] getFileIds() {
		return fileIds;
	}

	/**
	 * Set a new length for the files of this archive
	 * @param length The length to set.
	 */
	public void setFiles(int length) {
		files = new File[length];
	}

	/**
	 * Get the files of this archive.
	 * @return {@code files}
	 */
	public File[] getFiles() {
		return files;
	}

	/**
	 * Get the id of a file by its name.
	 * @param name The name.
	 * @return The file id of the argued name.
	 */
	public int getFileId(String name) {
		for (final File file : files) {
			if (name != null && file.getName() == name.toLowerCase().hashCode()) {
				return file.getId();
			}
		}
		return -1;
	}

	/**
	 * Get a single file.
	 * @param id The id of the file to get.
	 * @return The file instance.
	 */
	public File getFile(int id) {
		for (final File file : files) {
			if (file.getId() == id) {
				return file;
			}
		}
		return null;
	}

	/**
	 * Get a file by its name.
	 * @param name The name of the file.
	 * @return The file instance.
	 */
	public File getFile(String name) {
		return getFile(getFileId(name));
	}

	/**
	 * Get the last file.
	 * @return The last file of this archive.
	 */
	public File getLastFile() {
		return files.length == 0 ? null : files[files.length - 1];
	}

	/**
	 * Set if this archive has been read.
	 * @param read If this archive has been read.
	 */
	public void setIsRead(boolean read) {
		this.read = read;
	}

	/**
	 * Check if this archive has been read.
	 * @return {@code read}
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * If this archive is requiring an update.
	 * @return {@code needUpdate}
	 */
	public boolean isUpdateRequired() {
		return needUpdate;
	}

	/**
	 * Set if this archive needs to be updated.
	 * This method should not be used, instead use the {@code flag} method;
	 * @param needUpdate If we need to update this archive.
	 */
	@Deprecated
	public void setIsUpdateRequired(boolean needUpdate) {
		this.needUpdate = needUpdate;
	}

	/**
	 * Set if this archive is a new added archive.
	 * @param isNew If this archive is new.
	 */
	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * Check if this archive is a new archive that has been added.
	 * @return {@code isNew}
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * Get the info about this archive.
	 * @return The info.
	 */
	public String getInfo() {
		return "Archive[id=" + id + ", name=" + name + ", revision=" + revision + ", read=" + read + ", files=" + Arrays.toString(fileIds) + "]";
	}

	@Override
	public String toString() {
		return "Archive " + id;
	}

}