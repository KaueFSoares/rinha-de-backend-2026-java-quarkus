package br.kauesoares.data;

public class VectorDataset {

    static final int DIM = 14;

    public final float[] vectors;
    public final byte[] flags;
    public final int size;

    public VectorDataset(float[] vectors, byte[] flags, int size) {
        this.vectors = vectors;
        this.flags = flags;
        this.size = size;
    }
}