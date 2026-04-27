package br.kauesoares.dto;

public class ScoreResponse {
    public boolean approved;
    public float fraud_score;

    public ScoreResponse(boolean approved, float fraudScore) {
        this.approved = approved;
        this.fraud_score = fraudScore;
    }
}