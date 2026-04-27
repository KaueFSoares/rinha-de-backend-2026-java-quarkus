package br.kauesoares.data;

public class VectorStore {

    static final int DIM = 14;

    public static float[] vectors;
    public static byte[] flags;
    public static int size;

    public static void load(float[] v, byte[] f, int s) {
        vectors = v;
        flags = f;
        size = s;

        System.out.println("VectorStore loaded with " + s + " vectors");
    }
}