package br.kauesoares.data;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class DataLoader {

    static final int DIM = 14;

    void onStart(@Observes StartupEvent ev) {

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

            VectorStore.load(vectors, flags, N);

            System.out.println("Loaded " + N + " vectors");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}