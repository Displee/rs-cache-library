package org.displee.cache.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;

import org.displee.CacheLibrary;
import org.displee.CacheLibraryMode;
import org.displee.cache.index.archive.Archive;
import org.displee.cache.index.archive.Archive317;
import org.displee.cache.index.archive.ArchiveSector;
import org.displee.cache.index.archive.file.File;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.progress.ProgressListener;
import org.displee.utilities.*;

/**
 * A class that represents a single index inside the cache.
 * @author Displee
 */
public class Index317 extends Index {

	private static final String[] VERSION_NAMES = { "model_version", "anim_version", "midi_version", "map_version" };

	private static final String[] CRC_NAMES = { "model_crc", "anim_crc", "midi_crc", "map_crc" };

	private static final String[] INDEX_NAMES = { "model_index", "anim_index", "midi_index", "map_index" };

	/**
	 * Constructs a new {@code Index} {@code Object}.
	 * @param id The id of this index.
	 * @param randomAccessFile The {@link RandomAccessFile} of this index.
	 */
	public Index317(CacheLibrary origin, int id, RandomAccessFile randomAccessFile) {
		super(origin, id, randomAccessFile);
	}

	@Override
	public boolean update(ProgressListener listener, Map<Integer, int[]> map) {
		boolean updateChecksumTable = false;
		int updateCount = 0;
		for(Archive archive : archives) {
			if (archive == null || !archive.isUpdateRequired()) {
				continue;
			}
			updateCount++;
		}
		double i = 0;
		for (Archive archive : archives) {
			if (archive == null || !archive.isUpdateRequired()) {
				continue;
			}
			i++;
			archive.unFlag();
			if (!updateChecksumTable) {
				updateChecksumTable = true;
			}
			if (listener != null) {
				listener.notify((i / updateCount) * 80.0, "Repacking archive " + archive.getId() + "...");
			}
			final byte[] compressed = archive.write(new OutputStream(1024));
			archive.setCRC(HashGenerator.getCRCHash(compressed));
			archive.setWhirlpool(Whirlpool.getHash(compressed));
			final ArchiveSector backup = readArchiveSector(archive.getId());
			if (!writeArchiveSector(archive.getId(), compressed)) {
				System.err.println("Could not write the archive sector for index[id=" + super.id + ", archive=" + archive.getId() + "]");
				System.err.println("Reverting changes...");
				if (backup != null) {
					if (writeArchiveSector(archive.getId(), backup.getData())) {
						System.out.println("Changes have been reverted.");
					} else {
						System.err.println("Your cache is corrupt.");
					}
				}
				return false;
			}
			if (origin.getMode() == CacheLibraryMode.UN_CACHED) {
				archive.restore();
			}
		}
		if (id != 0 && id < VERSION_NAMES.length && updateChecksumTable) {
			writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::getRevision).toArray(), VERSION_NAMES[id -1], 1);
			writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::getCRC).toArray(), CRC_NAMES[id - 1], 2);
			writeArchiveProperties(Arrays.stream(archives).mapToInt(e -> ((Archive317) e).getPriority()).toArray(), INDEX_NAMES[id - 1], id == 2 ? 1 : 0);
		}
		if (listener != null) {
			listener.notify(100, "Successfully updated index " + id + ".");
		}
		return true;
	}

	@Override
	protected boolean isIndexValid(int index) {
		return this.id == index - 1;
	}

	@Override
	protected int indexToWrite(int index) {
		return index + 1;
	}

	/**
	 * Read this index.
	 */
	@Override
	protected void read() {
		int archiveLength;
		try {
			archiveLength = (int) (getRandomAccessFile().length() / (long) Constants.INDEX_SIZE);
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		archiveIds = new int[archiveLength];
		archives = new Archive[archiveLength];
		int[] versions = null;
		int[] crcs = null;
		int[] priorities = null;
		if (id != 0 && id < VERSION_NAMES.length) {
			versions = readArchiveProperties(VERSION_NAMES[id - 1], 1);
			crcs = readArchiveProperties(CRC_NAMES[id - 1], 2);
			priorities = readArchiveProperties(INDEX_NAMES[id - 1], id == 2 ? 1 : 0);
		}
		for(int i = 0; i < archives.length; i++) {
			archiveIds[i] = i;
			Archive317 archive = (Archive317) (archives[i] = new Archive317(i));
			if (versions == null || crcs == null || i >= versions.length) {
				continue;
			}
			archive.setRevision(versions[i]);
			archive.setCRC(crcs[i]);
			if (priorities != null) {
				archive.setPriority(i < priorities.length ? priorities[i] : 0);
			}
		}
	}

	@Override
	public boolean read(InputStream inputStream) {
		return true;
	}

	@Override
	public byte[] write(OutputStream outputStream) {
		return null;
	}

	@Override
	public Archive getArchive(int id, int[] xtea, boolean direct) {
		if (origin.isClosed()) {
			return null;
		}
		for (final Archive archive : archives) {
			if (archive.getId() == id) {
				if (direct || archive.isRead() || archive.isNew()) {
					return archive;
				}
				final ArchiveSector archiveSector = origin.getIndex(this.id).readArchiveSector(id);
				if (archiveSector == null) {
					archive.setIsRead(true);
					archive.setIsNew(true);
					archive.reset();
					return archive;
				}
				((Archive317) archive).setCompressionType(this.id == 0 ? Compression.CompressionType.BZIP2 : Compression.CompressionType.GZIP);
				archive.read(new InputStream(archiveSector.getData()));
				return archive;
			}
		}
		return null;
	}

	@Override
	public Archive addArchive(int id, int name, boolean resetFiles) {
		Archive current = getArchive(id, true);
		if (current != null && !current.isRead() && !current.isNew() && !current.isUpdateRequired()) {
			current = getArchive(id);
		}
		if (current != null) {
			if (name != -1 && current.getName() != name) {
				if (current.getName() > 0) {
					archiveNames.set(archiveNames.indexOf(current.getName()), name);
				}
				current.setName(name);
			}
			if (resetFiles) {
				current.reset();
			}
			flag();
			current.flag();
			return current;
		}
		archiveIds = Arrays.copyOf(archiveIds, archiveIds.length + 1);
		archiveIds[archiveIds.length - 1] = id;
		archives = Arrays.copyOf(archives, archives.length + 1);
		final Archive317 archive = new Archive317(id, name);
		if (this.id != 0) {
			archive.setCompressionType(Compression.CompressionType.GZIP);
		}
		archive.reset();
		archive.setIsNew(true);
		archive.flag();
		archives[archives.length - 1] = archive;
		flag();
		return archive;
	}

	private int[] readArchiveProperties(String fileId, int type) {
		if (id == 0 || id == 4 || id > VERSION_NAMES.length) {
			return null;
		}
		Archive archive = origin.getIndex(0).getArchive(5);
		if (archive == null) {
			System.err.println("Missing archive 5 for " + fileId + ", type=" + type);
			return null;
		}
		File file = archive.getFile(fileId);
		if (file == null) {
			System.err.println("Missing file for " + fileId + ", type=" + type);
			return null;
		}
		byte[] data = file.getData();
		if (data == null) {
			System.err.println("Missing file data for " + fileId + ", type=" + type);
			return null;
		}
		InputStream buffer = new InputStream(data);
		int[] properties = new int[data.length / (type == 0 ? 1 : type == 1 ? 2 : 4)];
		switch(type) {
			case 0:
				for (int i = 0; i < properties.length; i++) {
					properties[i] = buffer.readUnsignedByte();
				}
				break;
			case 1:
				for (int i = 0; i < properties.length; i++) {
					properties[i] = buffer.readUnsignedShort();
				}
				break;
			case 2:
				for (int i = 0; i < properties.length; i++) {
					properties[i] = buffer.readInt();
				}
				break;
		}
		return properties;
	}

	private boolean writeArchiveProperties(int[] properties, String fileId, int type) {
		if (id == 0 || id == 4 || id > VERSION_NAMES.length) {
			return false;
		}
		OutputStream buffer = new OutputStream(properties.length);
		if (type == 0) {
			for (int i : properties) {
				buffer.writeByte(i);
			}
		} else if (type == 1) {
			for (int i : properties) {
				buffer.writeShort(i);
			}
		} else if (type == 2) {
			for (int i : properties) {
				buffer.writeInt(i);
			}
		}
		origin.getIndex(0).getArchive(5).addFile(fileId, buffer.flip());
		return origin.getIndex(0).update();
	}

}
