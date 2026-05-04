package br.kauesoares;

import br.kauesoares.data.VectorDataset;

public class VectorSearch {

    private final VectorDataset vectorDataset;

    public VectorSearch(VectorDataset vectorDataset) {
        this.vectorDataset = vectorDataset;
    }

    public void top5(float[] q, int[] outIdx) {

        float q0 = q[0], q1 = q[1], q2 = q[2], q3 = q[3], q4 = q[4];
        float q5 = q[5], q6 = q[6], q7 = q[7], q8 = q[8], q9 = q[9];
        float q10 = q[10], q11 = q[11], q12 = q[12], q13 = q[13];

        float b0 = -1, b1 = -1, b2 = -1, b3 = -1, b4 = -1;
        int i0 = 0, i1 = 0, i2 = 0, i3 = 0, i4 = 0;

        float[] v = vectorDataset.vectors;

        for (int i = 0, base = 0; i < vectorDataset.size; i++, base += 14) {

            float s =
                    q0 * v[base] + q1 * v[base + 1] +
                            q2 * v[base + 2] + q3 * v[base + 3] +
                            q4 * v[base + 4] + q5 * v[base + 5] +
                            q6 * v[base + 6] + q7 * v[base + 7] +
                            q8 * v[base + 8] + q9 * v[base + 9] +
                            q10 * v[base + 10] + q11 * v[base + 11] +
                            q12 * v[base + 12] + q13 * v[base + 13];

            if (s > b0) {
                b4 = b3;
                i4 = i3;
                b3 = b2;
                i3 = i2;
                b2 = b1;
                i2 = i1;
                b1 = b0;
                i1 = i0;
                b0 = s;
                i0 = i;
            } else if (s > b1) {
                b4 = b3;
                i4 = i3;
                b3 = b2;
                i3 = i2;
                b2 = b1;
                i2 = i1;
                b1 = s;
                i1 = i;
            } else if (s > b2) {
                b4 = b3;
                i4 = i3;
                b3 = b2;
                i3 = i2;
                b2 = s;
                i2 = i;
            } else if (s > b3) {
                b4 = b3;
                i4 = i3;
                b3 = s;
                i3 = i;
            } else if (s > b4) {
                b4 = s;
                i4 = i;
            }
        }

        outIdx[0] = i0;
        outIdx[1] = i1;
        outIdx[2] = i2;
        outIdx[3] = i3;
        outIdx[4] = i4;
    }
}