import org.displee.CacheLibrary;
import org.displee.CacheLibraryMode;

public class Test {

	public static void main(String[] args) throws Exception {
		CacheLibrary lib = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS3\\861\\861 Cache editing", CacheLibraryMode.UN_CACHED);
		System.out.println(lib.getLastIndex());
	}

}
