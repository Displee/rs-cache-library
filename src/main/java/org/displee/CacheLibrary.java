package org.displee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.displee.cache.index.ChecksumTable;
import org.displee.cache.index.Index;
import org.displee.cache.index.Index317;
import org.displee.io.impl.OutputStream;
import org.displee.progress.ProgressListener;
import org.displee.utilities.Compression;
import org.displee.utilities.Compression.CompressionType;
import org.displee.utilities.Constants;

/**
 * A class that represents the main entry point of this cache library.
 * @author Displee
 */
public class CacheLibrary {

	/**
	 * An array of indices of this cache.
	 */
	private Index[] indices;

	/**
	 * The main random access file.
	 */
	private RandomAccessFile mainFile;

	/**
	 * The checksum table.
	 */
	private ChecksumTable checksumTable;

	/**
	 * The path to the cache files.
	 */
	private final String path;

	/**
	 * The mode.
	 */
	private final CacheLibraryMode mode;

	/**
	 * If this library has been closed.
	 */
	private boolean closed;

	/**
	 * Constructs a new {@code CacheLibrary} {@code Object}.
	 * @param path The path to the cache files.
	 * @throws IOException If it failed to read the cache files.
	 */
	public CacheLibrary(String path) throws IOException {
		this(path, CacheLibraryMode.CACHED);
	}

	/**
	 * Constructs a new {@code CacheLibrary} {@code Object}.
	 * @param path The path to the cache files.
	 * @param mode The cache library mode.
	 * @throws IOException If it failed to read the cache files.
	 */
	public CacheLibrary(String path, CacheLibraryMode mode) throws IOException {
		this(path, mode, null);
	}

	/**
	 * Constructs a new {@code CacheLibrary} {@code Object}.
	 * @param path The path to the cache files.
	 * @param mode The cache library mode.
	 * @param listener The progress listener.
	 * @throws IOException If it failed to read the cache files.
	 */
	public CacheLibrary(String path, CacheLibraryMode mode, ProgressListener listener) throws IOException {
		if (path == null) {
			throw new FileNotFoundException("The path to the cache is null.");
		}
		if (!path.endsWith("/") && !path.endsWith("\\")) {
			path += "/";
		}
		this.path = path;
		this.mode = mode;
		final File file = new File(path + "main_file_cache.dat");
		if (file.exists() && file.length() != 0) {
			load317(listener);
			return;
		}
		load(listener);
	}

	/**
	 * Create a new cache library in cached mode.
	 * @param path The path to the cache.
	 * @return The cache library.
	 */
	public static CacheLibrary create(String path) {
		return create(path, CacheLibraryMode.CACHED, null);
	}

	/**
	 * Create a new cache library uncached mode.
	 * @param path The path to the cache.
	 * @return The cache library.
	 */
	public static CacheLibrary createUncached(String path) {
		return create(path, CacheLibraryMode.UN_CACHED, null);
	}

	/**
	 * Create a new cache library.
	 * @param path The path to the cache.
	 * @param mode The cache library I/O mode.
	 * @param listener The progress listener.
	 * @return The cache library.
	 */
	public static CacheLibrary create(String path, CacheLibraryMode mode, ProgressListener listener) {
		try {
			return new CacheLibrary(path, mode, listener);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Load this cache.
	 * @param listener The progress listener.
	 * @throws IOException If it failed to read the cache files.
	 */
	private void load(ProgressListener listener) throws IOException {
		final File main = new File(path + "main_file_cache.dat2");
		if (main.exists()) {
			mainFile = new RandomAccessFile(main, "rw");
		} else {
			if (listener != null) {
				listener.notify(-1, "Error, main file could not be found");
			}
			throw new FileNotFoundException("File[path=" + main.getAbsolutePath() + "] could not be found.");
		}
		final File index255 = new File(path + "main_file_cache.idx255");
		if (!index255.exists()) {
			if (listener != null) {
				listener.notify(-1, "Error, checksum file could not be found.");
			}
			throw new FileNotFoundException("File[path=" + index255.getAbsolutePath() + "] could not be found.");
		}
		checksumTable = new ChecksumTable(this, 255, new RandomAccessFile(index255, "rw"));
		indices = new Index[(int) checksumTable.getRandomAccessFile().length() / Constants.INDEX_SIZE];
		if (listener != null) {
			listener.notify(0.0, "Reading indices...");
		}
		for(int i = 0; i < indices.length; i++) {
			final File file = new File(path, "main_file_cache.idx" + i);
			final double progress = (i / (indices.length - 1.0)) * 100;
			if (!file.exists()) {
				if (listener != null) {
					listener.notify(progress, "Could not load index " + i + ", missing idx file...");
				}
				continue;
			}
			try {
				indices[i] = new Index(this, i, new RandomAccessFile(file, "rw"));
				if (listener != null) {
					listener.notify(progress, "Loaded index " + i + " ...");
				}
			} catch(Exception e) {
				if (listener != null) {
					listener.notify(progress, "Failed to load index " + i + "...");
				}
				System.err.println("Failed to read index[id=" + i + ", file=" + file + ", length=" + file.length() + ", main=" + main + ", main_length=" + main.length() + ", indices=" + indices.length + "]");
				e.printStackTrace();
			}
		}
		checksumTable.write(new OutputStream(indices.length * Constants.ARCHIVE_HEADER_SIZE));
	}

	/**
	 * Load this cache.
	 * @param listener The progress listener.
	 * @throws IOException If it failed to read the cache files.
	 */
	private void load317(ProgressListener listener) throws IOException {
		final File main = new File(path + "main_file_cache.dat");
		if (main.exists()) {
			mainFile = new RandomAccessFile(main, "rw");
		} else {
			if (listener != null) {
				listener.notify(-1, "Error, main file could not be found");
			}
			throw new FileNotFoundException("File[path=" + main.getAbsolutePath() + "] could not be found.");
		}
		final File[] indexFiles = new File(path).listFiles((dir, name) -> {
			boolean list = name.startsWith("main_file_cache.idx");
			if (!list) {
				return false;
			}
			try(RandomAccessFile raf = new RandomAccessFile(new File(dir.getPath() + "/" + name), "r")) {
				return raf.length() != 0;
			} catch(Exception e) {
				return false;
			}
		});
		if (indexFiles == null || indexFiles.length == 0) {
			throw new IOException("No index files found.");
		}
		indices = new Index317[indexFiles.length];
		if (listener != null) {
			listener.notify(0.0, "Reading indices...");
		}
		for(int i = 0; i < indices.length; i++) {
			final File file = new File(path, "main_file_cache.idx" + i);
			final double progress = (i / (indices.length - 1.0)) * 100;
			if (!file.exists()) {
				if (listener != null) {
					listener.notify(progress, "Could not load index " + i + ", missing idx file...");
				}
				continue;
			}
			try {
				indices[i] = new Index317(this, i, new RandomAccessFile(file, "rw"));
				if (listener != null) {
					listener.notify(progress, "Loaded index " + i + " ...");
				}
			} catch(Exception e) {
				if (listener != null) {
					listener.notify(progress, "Failed to load index " + i + "...");
				}
				System.err.println("Failed to read index[id=" + i + ", file=" + file + ", length=" + file.length() + ", main=" + main + ", main_length=" + main.length() + ", indices=" + indices.length + "]");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add an index to this cache library.
	 * @param named If the index contains archive and/or file names.
	 * @param whirlpool If the index is using whirlpool.
	 */
	public Index addIndex(boolean named, boolean whirlpool) {
		try {
			if (is317()) {
				throw new UnsupportedOperationException("317 not supported to add new indices yet.");
			}
			final OutputStream outputStream = new OutputStream(4);
			outputStream.writeByte(5);
			outputStream.writeByte((named ? 0x1 : 0x0) | (whirlpool ? 0x2 : 0x0));
			outputStream.writeShort(0);
			final int id = indices.length;
			if (!checksumTable.writeArchiveSector(id, Compression.compress(outputStream.flip(), CompressionType.GZIP, null, -1))) {
				throw new RuntimeException("Failed to write the archive sector for a new index[id=" + id + "]");
			}
			indices = Arrays.copyOf(indices, indices.length + 1);
			Index index = indices[id] = new Index(this, id, new RandomAccessFile(new File(path + "main_file_cache.idx" + id), "rw"));
			index.flag();
			if (!index.update()) {
				throw new IOException("Unable to write CRC for the new index.");
			}
			return index;
		} catch(Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Remove the last index that is stored in the cache.
	 */
	public void removeLastIndex() {
		try {
			if (is317()) {
				throw new UnsupportedOperationException("317 not supported to remove indices yet.");
			}
			final int id = indices.length - 1;
			indices[id].getRandomAccessFile().close();
			final File file = new File(path, "main_file_cache.idx" + id);
			if (!file.exists() || !file.delete()) {
				throw new RuntimeException("Failed to remove the random access file of the argued index[id=" + id + ", file exists=" + file.exists() + "]");
			}
			checksumTable.getRandomAccessFile().setLength(id * Constants.INDEX_SIZE);
			indices = Arrays.copyOfRange(indices, 0, indices.length - 1);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Close this library from reading.
	 */
	public void close() {
		try {
			if (closed) {
				return;
			}
			mainFile.close();
			if (checksumTable != null) {
				checksumTable.getRandomAccessFile().close();
			}
			for(Index index : indices) {
				if (index != null && index.getRandomAccessFile() != null) {
					index.getRandomAccessFile().close();
				}
			}
			closed = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a single index from the cache.
	 * @param id The id of the index to get.
	 * @return The index instance.
	 */
	public Index getIndex(int id) {
		if (id >= indices.length) {
			return null;
		}
		return indices[id];
	}

	/**
	 * Uncache all indices.
	 */
	public void uncache() {
		for(Index index : indices) {
			index.uncache();
		}
	}

	/**
	 * Get the last index of this library.
	 * @return The last index.
	 */
	public Index getLastIndex() {
		if (indices.length == 0) {
			return null;
		}
		return indices[indices.length - 1];
	}

	/**
	 * Get the main random access file.
	 * @return {@code mainFile}
	 */
	public RandomAccessFile getMainFile() {
		return mainFile;
	}

	/**
	 * Get the indices as an array.
	 * @return {@code indices}
	 */
	public Index[] getIndices() {
		return indices;
	}

	/**
	 * Get the checksum table.
	 * @return {@code checksumTable}
	 */
	public ChecksumTable getChecksumTable() {
		return checksumTable;
	}

	/**
	 * Get the path.
	 * @return {@code path}
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the cache library mode.
	 * @return {@code mode}
	 */
	public CacheLibraryMode getMode() {
		return mode;
	}

	/**
	 * Check if this is a 317 cache.
	 * @return If this cache library is a 317 cache.
	 */
	public boolean is317() {
		return checksumTable == null;
	}

	/**
	 * Check if this is a OSRS cache.
	 * @return If the revision of index 2 is greater or equal to 300.
	 */
	public boolean isOSRS() {
		return getIndex(2).getRevision() >= 300 && indices.length <= 23;
	}

	/**
	 * Check if this is a RS3 cache.
	 * @return If the amount of indices is greater than 39.
	 */
	public boolean isRS3() {
		return indices.length > 39;
	}

	/**
	 * Check if this library has been closed.
	 * @return {@code }
	 */
	public boolean isClosed() {
		return closed;
	}

}
