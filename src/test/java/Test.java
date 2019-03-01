import org.displee.CacheLibrary;
import org.displee.CacheLibraryMode;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.Archive;
import org.displee.cache.index.archive.Archive317;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;
import org.displee.utilities.GZIPCompressor;
import org.displee.utilities.HashGenerator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Test {

	public static void main(String[] args) throws Exception {
		test1();
		//System.out.println("");
		//test2();
	}

	//0 = config, models = 1, animation = 2, midi = 3, maps = 4
	private static void test1() throws Exception {
		CacheLibrary cache = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\317\\317 cache", CacheLibraryMode.UN_CACHED);
		Index index = cache.getIndex(1);
		Archive archive = index.getArchive(9358);
		System.out.println(archive.getInfo());
	}

}
