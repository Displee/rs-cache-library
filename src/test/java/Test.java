import org.displee.CacheLibrary;
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;

import java.util.Arrays;

public class Test {

	public static void main(String[] args) throws Exception {
		CacheLibrary repository498 = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\RS2\\468");
		CacheLibrary repository179 = new CacheLibrary("C:\\Users\\Maffia\\Documents\\RSPS\\OSRS\\31\\data");

		System.out.println(Arrays.toString(repository498.getIndex(2).getArchiveIds()));
		System.out.println(Arrays.toString(repository179.getIndex(2).getArchiveIds()));
		System.out.println(repository498.getIndex(2).getArchiveIds().length);
	}

}
