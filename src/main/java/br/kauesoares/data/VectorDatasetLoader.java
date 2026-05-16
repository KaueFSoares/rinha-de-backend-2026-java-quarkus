package br.kauesoares.data;

import br.kauesoares.VectorSearch;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class VectorDatasetLoader {

    private static final int DIM = 14;
    private static final int RECORD_SIZE = DIM * 4 + 1;

    public VectorSearch load() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            URL url = cl.getResource("data.bin");
            if (url == null)
                throw new RuntimeException("data.bin not found");

            URLConnection conn = url.openConnection();
            long totalBytes = conn.getContentLengthLong();

            int totalRecords = (int) (totalBytes / RECORD_SIZE);

            float[] vectors = new float[totalRecords * DIM];
            byte[] flags = new byte[totalRecords];

            try (DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(url.openStream(), 1 << 20))) {

                for (int i = 0; i < totalRecords; i++) {
                    int base = i * DIM;

                    for (int j = 0; j < DIM; j++) {
                        vectors[base + j] = dis.readFloat();
                    }

                    flags[i] = dis.readByte();
                }
            }

            return new VectorSearch(vectors, flags, totalRecords);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}