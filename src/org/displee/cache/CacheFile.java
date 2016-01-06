package org.displee.cache;

import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;

/**
 * A class serving as interface for each index, archive and file.
 * @author Displee
 */
public interface CacheFile {

	/**
	 * Used to read data from an index, an archive or a file.
	 */
	public boolean read(InputStream inputStream);

	/**
	 * Write data to an index, archive or file.
	 */
	public byte[] write(OutputStream outputStream);

}