import org.displee.CacheLibrary;
import org.junit.Test;

import java.io.IOException;

public class TestOsrs {

	@Test
	public void checkCache435() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\435\\rs435_cache");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCache468() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\468");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCache498() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\498\\cache");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCache508() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\508\\508 Clean RS2HD\\data\\cache");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCache614() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\614\\runescape");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCache666() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\666\\cache");
		assert !repository.isOSRS();
	}

	@Test
	public void checkCacheOsrs31() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\31\\data");
		assert repository.isOSRS();
	}

	@Test
	public void checkCacheOsrs169() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\169\\#169 cache");
		assert repository.isOSRS();
	}

	@Test
	public void checkCacheOsrs177() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\177\\cache");
		assert repository.isOSRS();
	}

	@Test
	public void checkCacheOsrs178() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\178\\cache");
		assert repository.isOSRS();
	}

	@Test
	public void checkCacheOsrs179() throws IOException {
		CacheLibrary repository = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\179\\cache");
		assert repository.isOSRS();
	}

}
