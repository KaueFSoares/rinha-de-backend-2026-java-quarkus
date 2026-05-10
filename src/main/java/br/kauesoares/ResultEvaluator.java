package br.kauesoares;

import br.kauesoares.dto.ScoreResponse;

public class ResultEvaluator {

    private static final float THRESHOLD = 0.6f;

    public ScoreResponse evaluate(byte[] idx, ScoreResponse out) {
        int fraudCount = idx[0] + idx[1] + idx[2] + idx[3] + idx[4];

        out.fraudScore = fraudCount * 0.2f;
        out.approved = out.fraudScore < THRESHOLD;

        return out;
    }
}
