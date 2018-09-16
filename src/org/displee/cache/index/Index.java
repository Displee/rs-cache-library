package org.displee.cache.index;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.displee.CacheLibrary;
import org.displee.CacheLibraryMode;
import org.displee.cache.index.archive.Archive;
import org.displee.cache.index.archive.ArchiveInformation;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.progress.ProgressListener;
import org.displee.utilities.Compression;
import org.displee.utilities.Compression.CompressionTypes;
import org.displee.utilities.Constants;
import org.displee.utilities.HashGenerator;
import org.displee.utilities.Whirlpool;

/**
 * A class that represents a single index inside the cache.
 * @author Displee
 */
public class Index extends IndexInformation {

	/**
	 * The {@link RandomAccessFile} of this index.
	 */
	private RandomAccessFile randomAccessFile;

	/**
	 * The crc hash of this index.
	 */
	private int crc;

	/**
	 * The whirlpool hash of this index.
	 */
	private byte[] whirlpool;

	/**
	 * If this index has been cached.
	 */
	private boolean cached;

	/**
	 * The compression type.
	 */
	private CompressionTypes type;

	/**
	 * Constructs a new {@code Index} {@code Object}.
	 * @param id The id of this index.
	 * @param randomAccessFile The {@link RandomAccessFile} of this index.
	 */
	public Index(CacheLibrary origin, int id, RandomAccessFile randomAccessFile) {
		super(origin, id);
		this.randomAccessFile = randomAccessFile;
		read();
	}

	private void read() {
		if (id < 255) {
			final ArchiveInformation archiveInformation = super.origin.getChecksumTable().getArchiveInformation(id);
			if (archiveInformation != null) {
				crc = HashGenerator.getCRCHash(archiveInformation.getData());
				whirlpool = Whirlpool.getHash(archiveInformation.getData(), 0, archiveInformation.getData().length);
				super.read(new InputStream(Compression.decompress(archiveInformation, null)));
				type = archiveInformation.getCompression();
			} else {
				this.randomAccessFile = null;
			}
		}
	}

	public boolean update() {
		return update(null, null);
	}

	public boolean update(Map<Integer, int[]> map) {
		return update(null, map);
	}

	public boolean update(ProgressListener listener) {
		return update(listener, null);
	}

	/**
	 * Update all the archives and files in this index.
	 * @param listener The progress listener.
	 * @param map A map of xteas.
	 * @return If we have updated the cache with success {@code true}.
	 */
	public boolean update(ProgressListener listener, Map<Integer, int[]> map) {
		boolean updateChecksumTable = false;
		int updateCount = 0;
		for(Archive archive : archives) {
			if (archive == null) {
				continue;
			}
			updateCount++;
		}
		double i = 0;
		for (Archive archive : archives) {
			if (archive == null) {
				continue;
			}
			if (archive.isUpdateRequired()) {
				i++;
				archive.unFlag();
				int[] keys = map == null ? null : map.get(archive.getId());
				if (!updateChecksumTable) {
					updateChecksumTable = true;
				}
				if (listener != null) {
					listener.notify((i / updateCount) * 80.0, "Repacking archive " + archive.getId() + "...");
				}
				final byte[] compressed = Compression.compress(archive.write(new OutputStream(0)), super.id == 7 ? CompressionTypes.NONE : CompressionTypes.GZIP, keys, archive.getRevision());
				archive.setCRC(HashGenerator.getCRCHash(compressed, 0, compressed.length - 2));
				archive.setWhirlpool(Whirlpool.getHash(compressed, 0, compressed.length - 2));
				final ArchiveInformation backup = getArchiveInformation(archive.getId());
				if (!writeArchiveInformation(archive.getId(), compressed)) {
					System.err.println("Could not write the archive information for index[id=" + super.id + ", archive=" + archive.getId() + "]");
					System.err.println("Reverting changes...");
					if (backup != null) {
						if (writeArchiveInformation(archive.getId(), backup.getData())) {
							System.out.println("Changes have been reverted.");
						} else {
							System.err.println("Your cache is corrupt.");
						}
					}
					return false;
				}
			}
			if (origin.getMode() == CacheLibraryMode.UN_CACHED) {
				archive.restore();
			}
		}
		if (listener != null) {
			listener.notify(85, "Updating checksum table...");
		}
		if (updateChecksumTable || super.needUpdate) {
			super.revision++;
			final byte[] indexData = Compression.compress(super.write(new OutputStream()), type, null, -1);
			byte[] clonedData = indexData.clone();
			crc = HashGenerator.getCRCHash(clonedData);
			whirlpool = Whirlpool.getHash(clonedData, 0, clonedData.length);
			super.origin.getChecksumTable().writeArchiveInformation(super.id, indexData);
		}
		if (listener != null) {
			listener.notify(100, "Successfully updated index " + id + ".");
		}
		return true;
	}

	/**
	 * Get the archive information from this index.
	 * @param id The id of the archive to get.
	 * @return The archive information instance.
	 */
	public ArchiveInformation getArchiveInformation(int id) {
		synchronized (super.origin.getMainFile()) {
			try {
				int type = 0;
				if (id > 65535) {
					type = 1;
				}
				if (super.origin.getMainFile().length() < (Constants.INDEX_SIZE * id + Constants.INDEX_SIZE)) {
					throw new RuntimeException("File is too small.");
				}
				final byte[] buffer = new byte[Constants.ARCHIVE_SIZE];
				randomAccessFile.seek(Constants.INDEX_SIZE * id);
				randomAccessFile.read(buffer, 0, Constants.INDEX_SIZE);
				final InputStream inputStream = new InputStream(buffer);
				final ArchiveInformation archiveInformation = new ArchiveInformation(type, inputStream.read24BitInt(), inputStream.read24BitInt());
				if (archiveInformation.getSize() < 0 || archiveInformation.getSize() > 1000000) {
					return null;
				} else if (archiveInformation.getPosition() <= 0 || archiveInformation.getPosition() > (super.origin.getMainFile().length() / Constants.ARCHIVE_SIZE)) {
					return null;
				}
				int read = 0;
				int chunk = 0;
				int archiveDataSize = Constants.ARCHIVE_DATA_SIZE;
				int archiveHeaderSize = Constants.ARCHIVE_HEADER_SIZE;
				if (type == 1) {
					archiveDataSize -= 2;
					archiveHeaderSize += 2;
				}
				while (read < archiveInformation.getSize()) {
					if (archiveInformation.getPosition() == 0) {
						return null;
					}
					int requiredToRead = archiveInformation.getSize() - read;
					if (requiredToRead > archiveDataSize) {
						requiredToRead = archiveDataSize;
					}
					super.origin.getMainFile().seek(archiveInformation.getPosition() * Constants.ARCHIVE_SIZE);
					super.origin.getMainFile().read(inputStream.getBytes(), 0, requiredToRead + archiveHeaderSize);
					inputStream.setOffset(0);
					if (!archiveInformation.read(inputStream)) {
						throw new RuntimeException("Error, could not read the archive.");
					} else if (super.id != archiveInformation.getIndex() || id != archiveInformation.getId() || chunk != archiveInformation.getChunk()) {
						throw new RuntimeException("Error, the read data is incorrect. Data[currentIndex=" + super.id + ", index=" + archiveInformation.getIndex() + ", currentId=" + id + ", id=" + archiveInformation.getId() + ", currentChunk=" + chunk + ", chunk=" + archiveInformation.getChunk() + "]");
					} else if (archiveInformation.getNextPosition() < 0 || archiveInformation.getNextPosition() > (super.origin.getMainFile().length() / Constants.ARCHIVE_SIZE)) {
						throw new RuntimeException("Error, the next position is invalid.");
					}
					for (int i = 0; i < requiredToRead; i++) {
						archiveInformation.setData(read++, inputStream.getBytes()[i + archiveHeaderSize]);
					}
					archiveInformation.setPosition(archiveInformation.getNextPosition());
					chunk++;
				}
				return archiveInformation;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Write the archive information.
	 * @param id The id of the archive.
	 * @param data The data to write to this archive.
	 * @return If the archive information was written successfully.
	 */
	public boolean writeArchiveInformation(int id, byte[] data) {
		synchronized (super.origin.getMainFile()) {
			try {
				int position;
				Archive archive = null;
				ArchiveInformation archiveInformation = null;
				if (super.id != 255) {
					archive = getArchive(id, true);
				}
				boolean overWrite = (super.id == 255 && (archiveInformation = getArchiveInformation(id)) != null) || (archive != null && !archive.isNew());
				final byte[] buffer = new byte[Constants.ARCHIVE_SIZE];
				if (overWrite) {
					if (Constants.INDEX_SIZE * id + Constants.INDEX_SIZE > randomAccessFile.length()) {
						return false;
					}
					archiveInformation = getArchiveInformation(id);
					randomAccessFile.seek(id * Constants.INDEX_SIZE);
					randomAccessFile.read(buffer, 0, Constants.INDEX_SIZE);
					final InputStream inputStream = new InputStream(buffer);
					inputStream.setOffset(3);
					position = inputStream.read24BitInt();
					if (position <= 0 || position > super.origin.getMainFile().length() / Constants.ARCHIVE_SIZE) {
						return false;
					}
				} else {
					position = (int) ((super.origin.getMainFile().length() + (Constants.ARCHIVE_SIZE - 1)) / Constants.ARCHIVE_SIZE);
					if (position == 0) {
						position = 1;
					}
				}
				final OutputStream outputStream = new OutputStream();
				outputStream.write24BitInt(data.length);
				outputStream.write24BitInt(position);
				randomAccessFile.seek(id * Constants.INDEX_SIZE);
				randomAccessFile.write(outputStream.flip(), 0, Constants.INDEX_SIZE);
				int written = 0;
				int chunk = 0;
				int archiveDataSize = Constants.ARCHIVE_DATA_SIZE;
				int archiveHeaderSize = Constants.ARCHIVE_HEADER_SIZE;
				if (id > 65535) {
					archiveDataSize -= 2;
					archiveHeaderSize += 2;
				}
				while (written < data.length) {
					int currentPosition = 0;
					if (overWrite) {
						super.origin.getMainFile().seek(position * Constants.ARCHIVE_SIZE);
						super.origin.getMainFile().read(buffer, 0, archiveHeaderSize);
						archiveInformation.read(new InputStream(buffer));
						currentPosition = archiveInformation.getNextPosition();
						if (archiveInformation.getId() != id || chunk != archiveInformation.getChunk() || super.id != archiveInformation.getIndex()) {
							return false;
						}
						if (currentPosition < 0 || super.origin.getMainFile().length() / Constants.ARCHIVE_SIZE < currentPosition) {
							return false;
						}
					}
					if (currentPosition == 0) {
						overWrite = false;
						currentPosition = (int) ((super.origin.getMainFile().length() + (Constants.ARCHIVE_SIZE - 1)) / Constants.ARCHIVE_SIZE);
						if (currentPosition == 0) {
							currentPosition++;
						}
						if (currentPosition == position) {
							currentPosition++;
						}
					}
					if (data.length - written <= archiveDataSize) {
						currentPosition = 0;
					}
					if (archiveInformation == null) {
						archiveInformation = new ArchiveInformation(0, data.length, position, id, super.id);
					}
					archiveInformation.setType(archiveDataSize == 510 ? 1 : 0);
					archiveInformation.setChunk(chunk);
					archiveInformation.setPosition(currentPosition);
					super.origin.getMainFile().seek(position * Constants.ARCHIVE_SIZE);
					archiveInformation.write(new OutputStream(archiveHeaderSize));
					super.origin.getMainFile().write(archiveInformation.write(new OutputStream(archiveHeaderSize)), 0, archiveHeaderSize);
					int length = data.length - written;
					if (length > archiveDataSize) {
						length = archiveDataSize;
					}
					super.origin.getMainFile().write(data, written, length);
					written += length;
					position = currentPosition;
					chunk++;
				}
				return true;
			} catch (Throwable t) {
				t.printStackTrace();
				System.err.println("ERROR!");
				return false;
			}
		}
	}

	/**
	 * Cache all files of all archives in this index.
	 */
	public void cache() {
		cache(null);
	}

	/**
	 * Cache all files of all archives in this index with decoding the argued xteas.
	 * @param xteas An 2D array of xteas. Syntax: xteas[archive id] = xtea.
	 */
	public boolean cache(int[][] xteas)  {
		if (!cached) {
			for (final int archive : super.archiveIds) {
				try {
					super.getArchive(archive, xteas == null ? null : xteas[archive]);
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
			cached = true;
		}
		return cached;
	}

	public void uncache() {
		for (Archive archive : super.archives) {
			archive.restore();
		}
		cached = false;
	}

	/**
	 * Get the random access file.
	 * @return {@code randomAccessFile}
	 */
	public RandomAccessFile getRandomAccessFile() {
		return randomAccessFile;
	}

	/**
	 * Get the crc value of this index.
	 * @return The crc value.
	 */
	public int getCRC() {
		return crc;
	}

	/**
	 * Get the whirlpool of this index.
	 * @return {@code whirlpool}
	 */
	public byte[] getWhirlpool() {
		return whirlpool;
	}

	/**
	 * Set a new crc value for this index.
	 * @param crc The new crc value to set.
	 */
	public void setCRC(int crc) {
		this.crc = crc;
	}

	/**
	 * Geth the compression type.
	 * @return {@code type}
	 */
	public CompressionTypes getCompressionType() {
		return type;
	}

	/**
	 * Get the info about this index.
	 * @return The info.
	 */
	public String getInfo() {
		return "Index[id=" + id + "]";
	}

	@Override
	public String toString() {
		return "Index " + id;
	}

}