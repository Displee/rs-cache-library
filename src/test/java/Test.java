import org.displee.io.impl.InputStream;
import org.displee.io.impl.OutputStream;

public class Test {

	public static void main(String[] args) throws Exception {
		OutputStream stream = new OutputStream(5);
		stream.writeBigSmart(0);
		InputStream input = new InputStream(stream.flip());
		System.out.println(input.readBigSmart());
	}

}
