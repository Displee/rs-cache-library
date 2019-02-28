import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.Archive;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {

	public static void main(String[] args) throws Exception {
		test1();
		//System.out.println("");
		//test2();
	}

	//0 = config, models = 1, animation = 2, midi = 3, maps = 4
	private static void test1() throws Exception {
		CacheLibrary cache = CacheLibrary.createUncached("C:\\Users\\Maffia\\ExtinctMode2");
		for(Index index : cache.getIndices()) {
			System.out.println(index.getInfo());
		}
		Index index = cache.getIndex(1);
		Archive archive = index.getArchive(0);
		Files.write(Paths.get("wtf.dat"), archive.getFile(0).getData());
	}

}
