package org.displee.cache.index;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;

import org.displee.CacheLibrary;
import org.displee.cache.CacheFile;
import org.displee.cache.index.archive.Archive;
import org.displee.cache.index.archive.ArchiveInformation;
import org.displee.cache.index.archive.file.File;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.Compression;
import org.displee.utilities.Constants;

/**
 * A class that represents contains usefull data of this index.
 * @author Displee
 */
public class IndexInformation implements CacheFile {

	/**
	 * The id of the index.
	 */
	protected int id;

	/**
	 * The version.
	 */
	protected int version;

	/**
	 * The revision.
	 */
	protected int revision;

	/**
	 * If this index contains archive names.
	 */
	protected boolean named;

	/**
	 * If this index is using whirlpool.
	 */
	protected boolean whirlpool;

	/**
	 * An array of archive ids.
	 */
	protected int[] archiveIds;

	/**
	 * An array of real archives inside this index.
	 */
	protected Archive[] archives;

	/**
	 * If this index needs to be updated.
	 */
	protected boolean needUpdate;

	/**
	 * The cache library.
	 */
	protected CacheLibrary origin;

	/**
	 * Constructs a new {@code IndexInformation} {@code Object}.
	 * @param id The id of the index.
	 */
	public IndexInformation(CacheLibrary origin, int id) {
		this.origin = origin;
		this.id = id;
	}

	@Override
	public boolean read(InputStream inputStream) {
		version = inputStream.readByte() & 0xFF;
		if (version < 5 || version > 7) {
			throw new RuntimeException("Unknown index information version=" + version);
		}
		revision = version >= 6 ? inputStream.readInt() : 0;
		final int flag = inputStream.readByte();
		named = (flag & 0x1) != 0;
		whirlpool = (flag & 0x2) != 0;
		archiveIds = new int[version >= 7 ? inputStream.readBigSmart() : (inputStream.readShort() & 0xFFFF)];
		int lastArchiveId = 0;
		for (int i = 0; i < archiveIds.length; i++) {
			archiveIds[i] = (version >= 7 ? inputStream.readBigSmart() : (inputStream.readShort() & 0xFFFF)) + (i == 0 ? 0 : archiveIds[i - 1]);
			if (archiveIds[i] > lastArchiveId) {
				lastArchiveId = archiveIds[i];
			}
		}
		archives = new Archive[archiveIds.length];
		for (int i = 0; i < archives.length; i++) {
			archives[i] = new Archive(archiveIds[i]);
		}
		if (named) {
			for(int i = 0; i < archives.length; i++) {
				archives[i].setName(inputStream.readInt());
			}
		}
		if (whirlpool) {
			for (int i = 0; i < archives.length; i++) {
				inputStream.readBytes(archives[i].getWhirlpool(), 0, Constants.WHIRLPOOL_SIZE);
			}
		}
		for (int i = 0; i < archives.length; i++) {
			archives[i].setCRC(inputStream.readInt());
		}
		for (int i = 0; i < archives.length; i++) {
			archives[i].setRevision(inputStream.readInt());
		}
		for (int i = 0; i < archives.length; i++) {
			archives[i].setFileIds(version >= 7 ? inputStream.readBigSmart() : (inputStream.readShort() & 0xFFFF));
		}
		for(int i = 0; i < archives.length; i++) {
			int lastFileId = -1;
			final Archive archive = archives[i];
			for(int fileIndex = 0; fileIndex < archive.getFileIds().length; fileIndex++) {
				archive.getFileIds()[fileIndex] = (version >= 7 ? inputStream.readBigSmart() : (inputStream.readShort() & 0xFFFF)) + (fileIndex == 0 ? 0 : archive.getFileIds()[fileIndex - 1]);
				if (archive.getFileIds()[fileIndex] > lastFileId) {
					lastFileId = archive.getFileIds()[fileIndex];
				}
			}
			archive.setFiles(archive.getFileIds().length);
			for (int fileIndex = 0; fileIndex < archive.getFileIds().length; fileIndex++) {
				archive.getFiles()[fileIndex] = new File(archive.getFileIds()[fileIndex]);
			}
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				final Archive archive = archives[i];
				for (int fileIndex = 0; fileIndex < archive.getFileIds().length; fileIndex++) {
					archive.getFile(archive.getFileIds()[fileIndex]).setName(inputStream.readInt());
				}
			}
		}
		return true;
	}

	@Override
	public byte[] write(OutputStream outputStream) {
		outputStream.writeByte(version);
		if (version >= 6) {
			outputStream.writeInt(revision);
		}
		outputStream.writeByte((named ? 0x1 : 0x0) | (whirlpool ? 0x2 : 0x0));
		if (version >= 7) {
			outputStream.writeBigSmart(archives.length);
		} else {
			outputStream.writeShort(archives.length);
		}
		for (int i = 0; i < archives.length; i++) {
			if (version >= 7) {
				outputStream.writeBigSmart(archiveIds[i] - (i == 0 ? 0 : archiveIds[i - 1]));
			} else {
				outputStream.writeShort(archiveIds[i] - (i == 0 ? 0 : archiveIds[i - 1]));
			}
		}
		if (named) {
			for(int i = 0; i < archives.length; i++) {
				outputStream.writeInt(archives[i].getName());
			}
		}
		if (whirlpool) {
			for (int i = 0; i < archives.length; i++) {
				outputStream.writeBytes(archives[i].getWhirlpool());
			}
		}
		for (int i = 0; i < archives.length; i++) {
			outputStream.writeInt(archives[i].getCRC());
		}
		for (int i = 0; i < archives.length; i++) {
			outputStream.writeInt(archives[i].getRevision());
		}
		for (int i = 0; i < archives.length; i++) {
			if (version >= 7) {
				outputStream.writeBigSmart(archives[i].getFileIds().length);
			} else {
				outputStream.writeShort(archives[i].getFileIds().length);
			}
		}
		for(int i = 0; i < archives.length; i++) {
			final Archive archive = archives[i];
			for (int fileIndex = 0; fileIndex < archive.getFileIds().length; fileIndex++) {
				if (version >= 7) {
					outputStream.writeBigSmart(archive.getFileIds()[fileIndex] - (fileIndex == 0 ? 0 : archive.getFileIds()[fileIndex - 1]));
				} else {
					outputStream.writeShort(archive.getFileIds()[fileIndex] - (fileIndex == 0 ? 0 : archive.getFileIds()[fileIndex - 1]));
				}
			}
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				final Archive archive = archives[i];
				for (int fileIndex = 0; fileIndex < archive.getFileIds().length; fileIndex++) {
					outputStream.writeInt(archive.getFile(archive.getFileIds()[fileIndex]).getName());
				}
			}
		}
		return outputStream.flip();
	}

	/**
	 * Reset this index.
	 */
	public void reset() {
		archiveIds = new int[0];
		archives = new Archive[0];
	}

	/**
	 * Copy this index.
	 * @return The new index.
	 */
	public IndexInformation copy() {
		final IndexInformation indexInformation = new IndexInformation(origin, id);
		indexInformation.version = version;
		indexInformation.revision = revision;
		indexInformation.named = named;
		indexInformation.archiveIds = archiveIds;
		indexInformation.archives = archives;
		indexInformation.whirlpool = whirlpool;
		return indexInformation;
	}

	/**
	 * Get the id of this index descriptor.
	 * @return {@code id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set a new id for this index.
	 * @param id The new index to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the version.
	 * @return {@code version}
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Get the revision.
	 * @return {@code revision}
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Set a new value for the revision.
	 * @param revision The new value.
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * Get the archive ids.
	 * @return {@code archiveIds}
	 */
	public int[] getArchiveIds() {
		return archiveIds;
	}

	/**
	 * Get the archives.
	 * @return {@code archives}
	 */
	public Archive[] getArchives() {
		return archives;
	}

	/**
	 * Add mulitple archives to this index.
	 * @param archives An array of archives.
	 * @param addFiles If we need to add the files in the archives to the new archive.
	 * @param resetFiles If we need to reset all the files in the archives.
	 */
	public void addArchives(Archive[] archives, boolean addFiles, boolean resetFiles) {
		for(Archive archive : archives) {
			addArchive(archive, addFiles, resetFiles);
		}
	}
	
	/**
	 * Add a archive instance to this index.
	 * @param archive The archive to add.
	 * @param resetFiles If we need to reset all files in the new archive.
	 * @return The new archive instance.
	 */
	public Archive addArchive(Archive archive, boolean resetFiles) {
		return addArchive(archive, true, resetFiles, getLastArchive().getId() + 1);
	}
	
	/**
	 * Add an archive instance to this index.
	 * @param archive The archive instance.
	 * @param addFiles If we need to add the files in this archive to the new archive.
	 * @param resetFiles If we need to reset all the files in the new archive.
	 * @return The new archive instance.
	 */
	public Archive addArchive(Archive archive, boolean addFiles, boolean resetFiles) {
		return addArchive(archive, addFiles, resetFiles, archive.getId());
	}

	/**
	 * Add an archive instance to this index.
	 * @param archive The archive instance.
	 * @param addFiles If we need to add the files in this archive to the new archive.
	 * @param resetFiles If we need to reset all the files in the new archive.
	 * @param id The id to give to the new archive.
	 * @return The new archive instance.
	 */
	public Archive addArchive(Archive archive, boolean addFiles, boolean resetFiles, int id) {
		final File[] files = archive.getFiles();
		final Archive newArchive = addArchive(archive.getId(), archive.getName(), resetFiles);
		if (addFiles) {
			newArchive.addFiles(files);
			newArchive.setIsRead(true);
		}
		return newArchive;
	}
	
	/**
	 * Add an archive to this index.
	 * @return The new added archive.
	 */
	public Archive addArchive() {
		return addArchive(getLastArchive().getId() + 1);
	}
	
	/**
	 * Add a new archive to this index.
	 * @param name The name.
	 * @return The archive instance.
	 */
	public Archive addArchive(String name) {
		return addArchive(name, false);
	}

	/**
	 * Add a new archive to this index.
	 * @param name The name of the archive to add.
	 * @param data The data.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive addArchive(String name, boolean resetFiles) {
		final int archiveId = getArchiveId(name);
		return addArchive(archiveId == -1 ? (getLastArchive().getId() + 1) : archiveId, name == null ? -1 : name.toLowerCase().hashCode(), resetFiles);
	}

	/**
	 * Add a new archive to this index.
	 * @param id The id of the new archive.
	 * @return The archive instance.
	 */
	public Archive addArchive(int id) {
		return addArchive(id, false);
	}

	/**
	 * Add a new archive to this index.
	 * @param id The id of the new archive.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive addArchive(int id, boolean resetFiles) {
		return addArchive(id, -1, resetFiles);
	}

	/**
	 * Add a new archive to this index.
	 * @param id The id of the new archive.
	 * @param data The data.
	 * @param name The archive name.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive addArchive(int id, int name, boolean resetFiles) {
		final Archive current = getArchive(id, true);
		if (current != null) {
			if (current.getName() != -1) {
				current.setName(name);
			}
			current.setIsUpdateRequired(true);
			if (resetFiles) {
				current.reset();
			}
			flag();
			return current;
		}
		archiveIds = Arrays.copyOf(archiveIds, archiveIds.length + 1);
		archiveIds[archiveIds.length - 1] = id;
		archives = Arrays.copyOf(archives, archives.length + 1);
		final Archive archive = new Archive(id, name);
		archive.setIsUpdateRequired(true);
		archive.reset();
		archive.setIsNew(true);
		archives[archives.length - 1] = archive;
		flag();
		return archive;
	}

	/**
	 * Remove an archive by its name.
	 * @param name The name of the archive to remove.
	 */
	public void removeArchive(String name) {
		removeArchive(getArchiveId(name));
	}

	/**
	 * Remove an archive.
	 * @param id The id of the archive to remove.
	 */
	public void removeArchive(int id) {
		try {
			boolean exists = false;
			for(final Archive archive : archives) {
				if (archive.getId() == id) {
					exists = true;
				}
			}
			if (!exists) {
				throw new FileNotFoundException("The argued archive id=" + id + " could not be removed as it is not stored in this index.");
			}
			final int[] archiveIds = new int[this.archiveIds.length - 1];
			int offset = 0;
			for(int i = 0; i < this.archiveIds.length; i++) {
				if (this.archiveIds[i] != id) {
					archiveIds[offset++] = this.archiveIds[i];
				}
			}
			this.archiveIds = archiveIds;
			offset = 0;
			final Archive[] archives = new Archive[this.archives.length - 1];
			for(int i = 0; i < this.archives.length; i++) {
				if (this.archives[i].getId() != id) {
					archives[offset++] = this.archives[i];
				}
			}
			this.archives = archives;
			flag();
		} catch(Exception exception) {
			exception.printStackTrace();
			return;
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
	 * Sort the holders of this index.
	 */
	public void sort() {
		Arrays.sort(archiveIds);
		Arrays.sort(archives, new Comparator<Archive>() {
			@Override
			public int compare(Archive archive1, Archive archive2) {
				return archive1.getId() > archive2.getId() ? 0 : -1;
			}
		});
	}

	/**
	 * Get an archive instance by its name.
	 * @param name The name of the archive to get.
	 * @return The archive instance.
	 */
	public Archive getArchive(String name) {
		return getArchive(name, null);
	}

	/**
	 * Get an archive instance by its name and decoded by the argued xteas.
	 * @param name The name of the archive to get.
	 * @param xteas The xteas
	 * @return The archive instance.
	 */
	public Archive getArchive(String name, int[] xteas) {
		return getArchive(getArchiveId(name), xteas, false);
	}

	/**
	 * Get a single archive by its name.
	 * @param name The name of the archive.
	 * @param direct If we must get the archive instance without reading it.
	 * @return The archive instance.
	 */
	public Archive getArchive(String name, boolean direct) {
		return getArchive(getArchiveId(name), null, direct);
	}

	/**
	 * Get a single archive.
	 * @param id The id of the archive to get.
	 * @return The archive instance.
	 */
	public Archive getArchive(int id) {
		return getArchive(id, false);
	}

	/**
	 * Get a single archive with decrypting the argued xteas.
	 * @param id The id of the archive to get.
	 * @param xteas The xteas.
	 * @return The archive instance.
	 */
	public Archive getArchive(int id, int[] xteas) {
		return getArchive(id, xteas, false);
	}

	/**
	 * Get a single archive.
	 * @param id The id of the archive to get.
	 * @param direct If we want to get the instance without reading it.
	 * @return The archive instance.
	 */
	public Archive getArchive(int id, boolean direct) {
		return getArchive(id, null, direct);
	}

	/**
	 * Get a single archive.
	 * @param id The id of the archive to get.
	 * @param xtea An array of xteas.
	 * @param direct If we want to get the archive without reading it.
	 * @return The archive instance.
	 */
	public Archive getArchive(int id, int[] xtea, boolean direct) {
		for(final Archive archive : archives) {
			if (archive.getId() == id) {
				if (!archive.isRead() && !direct) {
					final ArchiveInformation archiveInformation = origin.getIndex(this.id).getArchiveInformation(id);
					if (archiveInformation != null) {
						archive.read(new InputStream(Compression.decompress(archiveInformation, xtea)));
						final InputStream inputStream = new InputStream(archiveInformation.getData());
						inputStream.setOffset(1);
						final int remaining = inputStream.getBytes().length - ((inputStream.readInt() & 0xFFFFFF) + inputStream.getOffset());
						if (remaining >= 2) {
							inputStream.setOffset(inputStream.getBytes().length - 2);
							archive.setRevision(inputStream.readShort() & 0xFFFF);
						}
					} else {
						return null;
					}
				}
				return archive;
			}
		}
		return null;
	}

	/**
	 * Get the id of an archive by its name.
	 * @param name The name.
	 * @return The archive id of the argued name.
	 */
	public int getArchiveId(String name) {
		for(final Archive archive : archives) {
			if (name != null && archive.getName() == name.toLowerCase().hashCode()) {
				return archive.getId();
			}
		}
		return -1;
	}

	/**
	 * Get the last archive.
	 * @return The last archive instance of this index.
	 */
	public Archive getLastArchive() {
		return archives[archives.length - 1];
	}

	/**
	 * Check if this index needs to be updated.
	 * @return {@code needUpdate}
	 */
	public boolean isUpdateRequired() {
		return needUpdate;
	}

	/**
	 * Check if this index is named.
	 * @return {@code named}
	 */
	public boolean isNamed() {
		return named;
	}

	/**
	 * Check if this index is using whirlpool
	 * @return {@code whirlpool}
	 */
	public boolean usingWhirlpool() {
		return whirlpool;
	}

	/**
	 * Get the origin.
	 * @return {@code origin}
	 */
	public CacheLibrary getOrigin() {
		return origin;
	}

	/**
	 * Set a new length for the {@code archives}.
	 * @param length The new length to set.
	 */
	public void setArchivesLength(int length) {
		archives = new Archive[length];
	}

}