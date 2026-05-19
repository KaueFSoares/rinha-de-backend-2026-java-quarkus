package br.kauesoares.data;

import br.kauesoares.VectorSearch;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class VectorDatasetLoader {

    private static final int DIM = 14;
    private static final int RECORD_SIZE = DIM * 4 + 1;

    public VectorSearch load() {
        try {
            File file = new File("/work/data.bin");

            long totalBytes = file.length();
            int totalRecords = (int) (totalBytes / RECORD_SIZE);

            float[] vectors = new float[totalRecords * DIM];
            byte[] flags = new byte[totalRecords];

            try (RandomAccessFile raf = new RandomAccessFile(file, "r");
                 FileChannel channel = raf.getChannel()) {

                MappedByteBuffer buffer =
                        channel.map(FileChannel.MapMode.READ_ONLY, 0, totalBytes);

                buffer.order(ByteOrder.BIG_ENDIAN);

                for (int i = 0; i < totalRecords; i++) {
                    int base = i * DIM;

                    for (int j = 0; j < DIM; j++) {
                        vectors[base + j] = buffer.getFloat();
                    }

                    flags[i] = buffer.get();
                }
            }

            return new VectorSearch(vectors, flags, totalRecords);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}