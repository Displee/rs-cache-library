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
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.progress.ProgressListener;
import org.displee.utilities.*;

/**
 * A class that represents a single index inside the cache.
 * @author Displee
 */
public class Index317 extends Index {

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
			int[] keys = map == null ? null : map.get(archive.getId());
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
		/*if (id != 0) {
			Archive versionArchive = origin.getIndex(0).getArchive(6);
			String[] versions = { "model_version", "anim_version", "midi_version", "map_version" };
			int length = versions.length;
			for(int i = 0; i < length; i++) {
				int index = i + 1;
				if (id == index) {
					InputStream buffer = new InputStream(versionArchive.getFile(versions[i]).getData());
					break;
				}
			}
		}*/
		int archiveLength;
		try {
			archiveLength = (int) (getRandomAccessFile().length() / (long) Constants.INDEX_SIZE);
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		archiveIds = new int[archiveLength];
		archives = new Archive317[archiveLength];
		for(int i = 0; i < archives.length; i++) {
			archiveIds[i] = i;
			archives[i] = new Archive317(i);
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
				if (direct || archive.isRead()) {
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
		final Archive archive = new Archive317(id, name);
		archive.reset();
		archive.setIsNew(true);
		archive.flag();
		archives[archives.length - 1] = archive;
		flag();
		return archive;
	}

}