package br.kauesoares.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VectorDatasetLoader {

    static final int DIM = 14;

    public VectorDataset load() {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("data.bin")) {
            if (is == null)
                throw new RuntimeException("data.bin not found");

            byte[] all = is.readAllBytes();

            int recordSize = DIM * 4 + 1;
            int N = all.length / recordSize;

            float[] vectors = new float[N * DIM];
            byte[] flags = new byte[N];

            ByteBuffer bb = ByteBuffer.wrap(all).order(ByteOrder.BIG_ENDIAN);

            for (int i = 0; i < N; i++) {
                int base = i * DIM;

                for (int j = 0; j < DIM; j++) {
                    vectors[base + j] = bb.getFloat();
                }

                flags[i] = bb.get();
            }

            return new VectorDataset(vectors, flags, N);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}