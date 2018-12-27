package org.displee.utilities;

import lzma.sdk.lzma.Encoder;
import lzma.streams.LzmaEncoderWrapper;
import lzma.streams.LzmaOutputStream;

import java.io.*;

import static org.displee.utilities.Miscellaneous.copy;

/**
 * An implementation the LZMA compression algorithm backed by Tukaani.xz which can be used for compression of data in
 * the RuneScape cache.
 * 
 * This is achieved by not writing the length of the data to the LZMA buffer, since in the RuneScape cache this length
 * is already provided in the container data.
 * 
 * @author Techdaan
 */
public class RSLZMAEncoder extends LzmaEncoderWrapper {

    /**
     * The wrapped encoder
     */
    private Encoder encoder;

    /**
     * Creates a new LZMA encoder
     */
    public RSLZMAEncoder() {
        super(null);

        this.encoder = new Encoder();

        encoder.setDictionarySize(8 << 20);
        encoder.setNumFastBytes(110);
        encoder.setMatchFinder(0);
        encoder.setLcLpPb(3, 0, 2);
        encoder.setEndMarkerMode(false);
    }

    @Override
    public void code(InputStream in, OutputStream out) throws IOException {
        encoder.writeCoderProperties(out);
        encoder.code(in, out, -1, -1, null);
    }

    /**
     * Compresses a block of data
     * @param data The data to compress
     * @return The LZMA compressed data
     * @throws IOException When something goes wrong in the compression
     */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        LzmaOutputStream lzma = new LzmaOutputStream(baos, new RSLZMAEncoder());
        try {
            copy(bais, lzma);
        } catch (IOException e) {
            throw e;
        } finally {
            baos.close();
            lzma.close();
            bais.close();
        }

        return baos.toByteArray();
    }

}