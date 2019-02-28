package org.displee;


/**
 * An enumeration of cache modes which can be useful depending on
 * the type of application where this cache library is being used in.
 * @author Displee
 */
public enum CacheLibraryMode {

	/**
	 * The cached mode.
	 * In this mode, the cache will hold the data of an index after it's being updated.
	 * So the next time you're retrieving data from this index, it will read the cached data.
	 * This is useful for caching data so you don't need to read the data again from the cache.
	 * This mode is recommended when using this cache library in a game server.
	 */
    CACHED,

	/**
	 * The uncached mode.
	 * In this mode, the cache will clear the data of an index after it's being updated.
	 * So the next time you're retrieving data from this index, it will read the cache again.
	 * This can be useful for if you want to be sure that you packed your data correctly by
	 * reading the same file again from the cache. This mode is recommended when using this cache library
	 * in a tool application.
	 */
    UN_CACHED

}
