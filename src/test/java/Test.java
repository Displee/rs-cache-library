
import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;

import java.util.Arrays;

public class Test {

	public static void main(String[] args) {
		debug(new byte[] { -65, -64 });
		debug(new byte[] { 0 });
	}

	private static void debug(byte[] data) {
		System.out.println("Input=" + Arrays.toString(data));
		InputStream input = new InputStream(data);
		int value = input.readSmart();
		System.out.println("Read value=" + value);

		OutputStream out = new OutputStream(2);
		out.writeSmart(value);
		System.out.println("Output=" + Arrays.toString(out.flip()));
	}

}
