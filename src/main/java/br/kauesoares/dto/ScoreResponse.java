package br.kauesoares.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ScoreResponse {
    public boolean approved;
    @JsonProperty("fraud_score")
    public float fraudScore;
}