package br.kauesoares.data;

public class MccRiskDataset {
    public float get(String mcc) {
        return switch (mcc) {
            case "5411" -> 0.15f;
            case "5812" -> 0.30f;
            case "5912" -> 0.20f;
            case "5944" -> 0.45f;
            case "7801" -> 0.80f;
            case "7802" -> 0.75f;
            case "7995" -> 0.85f;
            case "4511" -> 0.35f;
            case "5311" -> 0.25f;
            default -> 0.50f;
        };
    }
}
