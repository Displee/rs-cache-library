package org.displee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.displee.cache.index.ChecksumTable;
import org.displee.cache.index.Index;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.Compression;
import org.displee.utilities.Compression.CompressionTypes;
import org.displee.utilities.Constants;

/**
 * A class that represents the main entry point of this cache library.
 * @author Displee
 */
public class CacheLibrary {

	/**
	 * The revision of this cache library
	 */
	private final int revision;

	/**
	 * An array of indices of this cache.
	 */
	private Index[] indices;

	/**
	 * The main random access file (.dat2).
	 */
	private final RandomAccessFile mainFile;

	/**
	 * The checksum table.
	 */
	private final ChecksumTable checksumTable;

	/**
	 * The path to the cache files.
	 */
	private final String path;

	/**
	 * Initialize this cache library.
	 * @param path The path to the cache files.
	 * @throws Exception
	 */
	public CacheLibrary(String path) throws Exception {
		this(path, 562);
	}

	/**
	 * Initialize this cache library.
	 * @param path The path to the cache files.
	 * @param revision The revision of the cache files.
	 * @throws Exception
	 */
	public CacheLibrary(String path, int revision) throws Exception {
		try {
			if (path == null) {
				throw new FileNotFoundException("The path to the cache is incorrect.");
			}
			this.revision = revision;
			this.path = path;
			final File main = new File(path + "main_file_cache.dat2");
			if (!main.exists()) {
				throw new FileNotFoundException("File[path=" + main.getAbsolutePath() + "] could not be found.");
			} else {
				mainFile = new RandomAccessFile(main, "rw");
			}
			final File index255 = new File(path + "main_file_cache.idx255");
			if (!index255.exists()) {
				throw new FileNotFoundException("File[path=" + index255.getAbsolutePath() + "] could not be found.");
			}
			checksumTable = new ChecksumTable(this, 255, new RandomAccessFile(index255, "rw"));
			indices = new Index[(int) checksumTable.getRandomAccessFile().length() / Constants.INDEX_SIZE];
			for(int i = 0; i < indices.length; i++) {
				final File file = new File(path, "main_file_cache.idx" + i);
				if (file.exists()) {
					indices[i] = new Index(this, i, new RandomAccessFile(file, "rw"));
				}
			}
			checksumTable.write(new OutputStream(indices.length * 8));
		} catch(Exception exception) {
			throw exception;
		}
	}

	/**
	 * Add an index to this cache library.
	 * @param named If the index contains archive/file names.
	 * @param whirlpool If the index is using whirlpool.
	 */
	public Index addIndex(boolean named, boolean whirlpool) {
		try {
			final OutputStream outputStream = new OutputStream(4);
			outputStream.writeByte(5);
			outputStream.writeByte((named ? 0x1 : 0x0) | (whirlpool ? 0x2 : 0x0));
			outputStream.writeShort(0);
			final int id = indices.length;
			if (!checksumTable.writeArchiveInformation(id, Compression.compress(outputStream.flip(), CompressionTypes.GZIP, null, -1))) {
				throw new RuntimeException("Failed to write the archive information for a new index[id=" + id + "]");
			}
			indices = Arrays.copyOf(indices, indices.length + 1);
			return indices[id] = new Index(this, id, new RandomAccessFile(new File(path + "main_file_cache.idx" + id), "rw"));
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
			final int id = indices.length - 1;
			indices[id].getRandomAccessFile().close();
			final File file = new File(path, "main_file_cache.idx" + id);
			if (!file.exists() || !file.delete()) {
				throw new RuntimeException("Failed to remove the randomaccessfile of the argued index[id=" + id + ", file exists=" + file.exists() + "]");
			}
			checksumTable.getRandomAccessFile().setLength(id * Constants.INDEX_SIZE);
			int offset = 0;
			final Index[] newIndices = new Index[id];
			for(int i = 0; i < indices.length; i++) {
				if (indices[i].getId() != id) {
					newIndices[offset++] = indices[i];
				}
			}
			indices = newIndices;
		} catch(Exception exception) {
			exception.printStackTrace();
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
	 * Get the last index of this library.
	 * @return The last index.
	 */
	public Index getLastIndex() {
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
	 * Get the revision of this cache library.
	 * @return {@code revision}
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Get the path.
	 * @return {@code path}
	 */
	public String getPath() {
		return path;
	}

}