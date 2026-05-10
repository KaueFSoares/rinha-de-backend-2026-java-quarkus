package br.kauesoares;

import br.kauesoares.data.VectorDataset;

public class VectorSearch {

    private static final int DIM = 14;
    private static final int K = 5;
    private static final int LEAF_SIZE = 16;
    private static final int MAX_NODES = 1 << 20;

    private final float[] vectors;
    private final byte[] flags;
    private final int size;

    // VP-Tree structure (immutable after build, safe to share across threads)
    private final int[] vpNode;
    private final float[] vpThresh;
    private final int[] vpLeft;
    private final int[] vpRight;
    private final boolean[] vpLeaf;
    private int nodeCount;

    private final int[] leafPoints;
    private final int[] leafStart;
    private final int[] leafLen;

    private final int[] buildIndices;

    // Per-thread search state (avoids GC and concurrency issues)
    private static final class SearchState {
        final float[] resDist = new float[K + 1];
        final int[] resIdx = new int[K + 1];
        final int[] stack = new int[MAX_NODES];
        int resSize;
        float resTau;
        int stackTop;
    }

    private final ThreadLocal<SearchState> tlState =
            ThreadLocal.withInitial(SearchState::new);

    public VectorSearch(VectorDataset dataset) {
        this.vectors = dataset.vectors;
        this.flags = dataset.flags;
        this.size = dataset.size;

        vpNode = new int[MAX_NODES];
        vpThresh = new float[MAX_NODES];
        vpLeft = new int[MAX_NODES];
        vpRight = new int[MAX_NODES];
        vpLeaf = new boolean[MAX_NODES];
        leafStart = new int[MAX_NODES];
        leafLen = new int[MAX_NODES];
        leafPoints = new int[size];

        buildIndices = new int[size];
        for (int i = 0; i < size; i++) buildIndices[i] = i;

        buildVPTree(buildIndices, 0, size);
    }

    // ---- VP-Tree Build ----

    private int buildVPTree(int[] idx, int lo, int hi) {
        if (lo >= hi) return -1;

        int nodeId = nodeCount++;
        vpLeft[nodeId] = -1;
        vpRight[nodeId] = -1;
        vpLeaf[nodeId] = false;

        int count = hi - lo;

        if (count <= LEAF_SIZE) {
            vpLeaf[nodeId] = true;
            vpNode[nodeId] = idx[lo];
            leafStart[nodeId] = lo;
            leafLen[nodeId] = count;
            System.arraycopy(idx, lo, leafPoints, lo, count);
            return nodeId;
        }

        int vpIdx = idx[hi - 1];
        vpNode[nodeId] = vpIdx;

        int n = hi - 1 - lo;
        float[] dists = new float[n];
        for (int i = 0; i < n; i++) {
            dists[i] = euclideanSq(vpIdx, idx[lo + i]);
        }

        float median = selectMedian(dists, idx, lo, n);
        vpThresh[nodeId] = median;

        int mid = partitionByDist(idx, lo, hi - 1, vpIdx, median);

        vpLeft[nodeId] = buildVPTree(idx, lo, mid);
        vpRight[nodeId] = buildVPTree(idx, mid, hi - 1);

        return nodeId;
    }

    private float selectMedian(float[] dists, int[] idx, int lo, int n) {
        if (n == 0) return 0f;
        int medPos = n / 2;
        if (n <= 32) {
            for (int i = 1; i < n; i++) {
                float kd = dists[i];
                int ki = idx[lo + i];
                int j = i - 1;
                while (j >= 0 && dists[j] > kd) {
                    dists[j + 1] = dists[j];
                    idx[lo + j + 1] = idx[lo + j];
                    j--;
                }
                dists[j + 1] = kd;
                idx[lo + j + 1] = ki;
            }
        } else {
            quickSelectK(dists, idx, lo, 0, n - 1, medPos);
        }
        return dists[medPos];
    }

    private void quickSelectK(float[] dists, int[] idx, int idxOff,
                              int left, int right, int k) {
        while (left < right) {
            float pivot = dists[(left + right) >>> 1];
            int i = left, j = right;
            while (i <= j) {
                while (dists[i] < pivot) i++;
                while (dists[j] > pivot) j--;
                if (i <= j) {
                    float td = dists[i];
                    dists[i] = dists[j];
                    dists[j] = td;
                    int ti = idx[idxOff + i];
                    idx[idxOff + i] = idx[idxOff + j];
                    idx[idxOff + j] = ti;
                    i++;
                    j--;
                }
            }
            if (k <= j) right = j;
            else if (k >= i) left = i;
            else break;
        }
    }

    private int partitionByDist(int[] idx, int lo, int hi, int vp, float median) {
        int i = lo, j = hi - 1;
        while (i <= j) {
            while (i <= j && euclideanSq(vp, idx[i]) <= median) i++;
            while (i <= j && euclideanSq(vp, idx[j]) > median) j--;
            if (i < j) {
                int tmp = idx[i];
                idx[i] = idx[j];
                idx[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }

    // ---- Public search: top5 ----

    public void top5(float[] q, byte[] outFlags) {
        SearchState s = tlState.get();
        s.resSize = 0;
        s.resTau = Float.MAX_VALUE;

        searchIterative(q, s);

        // Drain max-heap into outFlags ascending (closest at [0]).
        int total = s.resSize;
        while (s.resSize > 0) {
            int pos = s.resSize - 1;
            outFlags[pos] = flags[s.resIdx[0]];
            s.resSize--;
            if (s.resSize > 0) {
                s.resDist[0] = s.resDist[s.resSize];
                s.resIdx[0] = s.resIdx[s.resSize];
                maxHeapSiftDown(s.resDist, s.resIdx, s.resSize);
            }
        }
        for (int i = total; i < K; i++) {
            outFlags[i] = 0;
        }
    }

    // ---- VP-Tree Search (iterative, per-thread state) ----

    private void searchIterative(float[] q, SearchState s) {
        s.stackTop = 0;
        s.stack[s.stackTop++] = 0;

        while (s.stackTop > 0) {
            int nodeId = s.stack[--s.stackTop];
            if (nodeId < 0) continue;

            if (vpLeaf[nodeId]) {
                int start = leafStart[nodeId];
                int len = leafLen[nodeId];
                for (int i = 0; i < len; i++) {
                    int pt = leafPoints[start + i];
                    float d = euclideanSqQ(q, pt);
                    if (s.resSize < K) {
                        maxHeapPush(s.resDist, s.resIdx, s.resSize, d, pt);
                        s.resSize++;
                        if (s.resSize == K) s.resTau = s.resDist[0];
                    } else if (d < s.resTau) {
                        s.resDist[0] = d;
                        s.resIdx[0] = pt;
                        maxHeapSiftDown(s.resDist, s.resIdx, s.resSize);
                        s.resTau = s.resDist[0];
                    }
                }
                continue;
            }

            int vp = vpNode[nodeId];
            float d = euclideanSqQ(q, vp);
            float thresh = vpThresh[nodeId];

            if (s.resSize < K) {
                maxHeapPush(s.resDist, s.resIdx, s.resSize, d, vp);
                s.resSize++;
                if (s.resSize == K) s.resTau = s.resDist[0];
            } else if (d < s.resTau) {
                s.resDist[0] = d;
                s.resIdx[0] = vp;
                maxHeapSiftDown(s.resDist, s.resIdx, s.resSize);
                s.resTau = s.resDist[0];
            }

            int left = vpLeft[nodeId];
            int right = vpRight[nodeId];

            if (d <= thresh) {
                if (s.resSize < K) {
                    if (right >= 0) s.stack[s.stackTop++] = right;
                    if (left >= 0) s.stack[s.stackTop++] = left;
                } else {
                    float gap = (float) Math.sqrt(thresh) - (float) Math.sqrt(d);
                    if (right >= 0 && gap <= (float) Math.sqrt(s.resTau)) {
                        s.stack[s.stackTop++] = right;
                    }
                    if (left >= 0) s.stack[s.stackTop++] = left;
                }
            } else {
                if (s.resSize < K) {
                    if (left >= 0) s.stack[s.stackTop++] = left;
                    if (right >= 0) s.stack[s.stackTop++] = right;
                } else {
                    float gap = (float) Math.sqrt(d) - (float) Math.sqrt(thresh);
                    if (left >= 0 && gap <= (float) Math.sqrt(s.resTau)) {
                        s.stack[s.stackTop++] = left;
                    }
                    if (right >= 0) s.stack[s.stackTop++] = right;
                }
            }
        }
    }

    // ---- Distance helpers ----

    private float euclideanSq(int a, int b) {
        int ba = a * DIM, bb = b * DIM;
        float[] v = vectors;
        float d0 = v[ba] - v[bb], d1 = v[ba + 1] - v[bb + 1],
                d2 = v[ba + 2] - v[bb + 2], d3 = v[ba + 3] - v[bb + 3],
                d4 = v[ba + 4] - v[bb + 4], d5 = v[ba + 5] - v[bb + 5],
                d6 = v[ba + 6] - v[bb + 6], d7 = v[ba + 7] - v[bb + 7],
                d8 = v[ba + 8] - v[bb + 8], d9 = v[ba + 9] - v[bb + 9],
                d10 = v[ba + 10] - v[bb + 10], d11 = v[ba + 11] - v[bb + 11],
                d12 = v[ba + 12] - v[bb + 12], d13 = v[ba + 13] - v[bb + 13];
        return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4 + d5 * d5 + d6 * d6 + d7 * d7
                + d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11 + d12 * d12 + d13 * d13;
    }

    private float euclideanSqQ(float[] q, int b) {
        int bb = b * DIM;
        float[] v = vectors;
        float d0 = q[0] - v[bb], d1 = q[1] - v[bb + 1],
                d2 = q[2] - v[bb + 2], d3 = q[3] - v[bb + 3],
                d4 = q[4] - v[bb + 4], d5 = q[5] - v[bb + 5],
                d6 = q[6] - v[bb + 6], d7 = q[7] - v[bb + 7],
                d8 = q[8] - v[bb + 8], d9 = q[9] - v[bb + 9],
                d10 = q[10] - v[bb + 10], d11 = q[11] - v[bb + 11],
                d12 = q[12] - v[bb + 12], d13 = q[13] - v[bb + 13];
        return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4 + d5 * d5 + d6 * d6 + d7 * d7
                + d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11 + d12 * d12 + d13 * d13;
    }

    // ---- Max-heap ----

    private static void maxHeapPush(float[] dist, int[] idx, int size, float d, int i) {
        dist[size] = d;
        idx[size] = i;
        int pos = size;
        while (pos > 0) {
            int parent = (pos - 1) >>> 1;
            if (dist[parent] >= dist[pos]) break;
            float td = dist[parent];
            dist[parent] = dist[pos];
            dist[pos] = td;
            int ti = idx[parent];
            idx[parent] = idx[pos];
            idx[pos] = ti;
            pos = parent;
        }
    }

    private static void maxHeapSiftDown(float[] dist, int[] idx, int size) {
        int pos = 0;
        while (true) {
            int l = (pos << 1) | 1, r = l + 1, largest = pos;
            if (l < size && dist[l] > dist[largest]) largest = l;
            if (r < size && dist[r] > dist[largest]) largest = r;
            if (largest == pos) break;
            float td = dist[pos];
            dist[pos] = dist[largest];
            dist[largest] = td;
            int ti = idx[pos];
            idx[pos] = idx[largest];
            idx[largest] = ti;
            pos = largest;
        }
    }
}