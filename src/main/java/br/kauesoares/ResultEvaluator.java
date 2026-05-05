package br.kauesoares;

import br.kauesoares.data.VectorDataset;
import br.kauesoares.dto.ScoreResponse;

public class ResultEvaluator {

    private static final float THRESHOLD = 0.6f;

    private final VectorDataset vectorDataset;

    public ResultEvaluator(VectorDataset vectorDataset) {
        this.vectorDataset = vectorDataset;
    }

    public ScoreResponse evaluate(int[] idx, ScoreResponse out) {
        int fraudCount =
                vectorDataset.flags[idx[0]] +
                        vectorDataset.flags[idx[1]] +
                        vectorDataset.flags[idx[2]] +
                        vectorDataset.flags[idx[3]] +
                        vectorDataset.flags[idx[4]];

        out.fraudScore = fraudCount * 0.2f;
        out.approved   = out.fraudScore < THRESHOLD;

        return out;
    }
}
