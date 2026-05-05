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

        float b0=Float.MAX_VALUE, b1=Float.MAX_VALUE, b2=Float.MAX_VALUE,
                b3=Float.MAX_VALUE, b4=Float.MAX_VALUE;
        int i0=0, i1=0, i2=0, i3=0, i4=0;

        float[] v = vectorDataset.vectors;
        for (int i = 0, base = 0; i < vectorDataset.size; i++, base += 14) {
            float d0=q0-v[base], d1=q1-v[base+1], d2=q2-v[base+2],
                    d3=q3-v[base+3], d4=q4-v[base+4], d5=q5-v[base+5],
                    d6=q6-v[base+6], d7=q7-v[base+7], d8=q8-v[base+8],
                    d9=q9-v[base+9], d10=q10-v[base+10], d11=q11-v[base+11],
                    d12=q12-v[base+12], d13=q13-v[base+13];
            float dist = d0*d0 + d1*d1 + d2*d2 + d3*d3 + d4*d4
                    + d5*d5 + d6*d6 + d7*d7 + d8*d8 + d9*d9
                    + d10*d10 + d11*d11 + d12*d12 + d13*d13;

            if (dist < b0) {
                b4=b3; i4=i3; b3=b2; i3=i2;
                b2=b1; i2=i1; b1=b0; i1=i0;
                b0=dist; i0=i;
            } else if (dist < b1) {
                b4=b3; i4=i3; b3=b2; i3=i2;
                b2=b1; i2=i1; b1=dist; i1=i;
            } else if (dist < b2) {
                b4=b3; i4=i3; b3=b2; i3=i2;
                b2=dist; i2=i;
            } else if (dist < b3) {
                b4=b3; i4=i3; b3=dist; i3=i;
            } else if (dist < b4) {
                b4=dist; i4=i;
            }
        }
        outIdx[0]=i0; outIdx[1]=i1; outIdx[2]=i2; outIdx[3]=i3; outIdx[4]=i4;
    }
}