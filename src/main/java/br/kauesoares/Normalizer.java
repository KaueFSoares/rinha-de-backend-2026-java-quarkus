package br.kauesoares;

import br.kauesoares.dto.Input;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Normalizer {

    static final float INV_MAX_AMOUNT = 1f / 10000f;
    static final float INV_MAX_INSTALLMENTS = 1f / 12f;
    static final float INV_AMOUNT_VS_AVG_RATIO = 1f / 10f;
    static final float INV_MAX_KM = 1f / 1000f;
    static final float INV_MAX_MINUTES = 1f / 1440f;
    static final float INV_MAX_TX = 1f / 20f;
    static final float INV_MAX_MERCHANT_AVG = 1f / 10000f;

    public void normalize(Input in, float[] out) {

        float amount = in.amount;
        float avg = in.customerAvg;
        float ratio = (avg > 0f) ? (amount / avg) : 0f;

        out[0] = clamp(amount * INV_MAX_AMOUNT);
        out[1] = clamp(in.installments * INV_MAX_INSTALLMENTS);
        out[2] = clamp((amount / ratio) * INV_AMOUNT_VS_AVG_RATIO);

        int hour = in.hour;
        out[3] = hour * (1f / 23f);

        out[4] = in.dayOfWeek * (1f / 6f);

        out[5] = in.hasLast ? clamp(in.minutesSince * INV_MAX_MINUTES) : -1f;
        out[6] = in.hasLast ? clamp(in.kmLast * INV_MAX_KM) : -1f;

        out[7] = clamp(in.kmHome * INV_MAX_KM);
        out[8] = clamp(in.tx24h * INV_MAX_TX);

        out[9] = in.isOnline ? 1f : 0f;
        out[10] = in.cardPresent ? 1f : 0f;

        out[11] = in.unknownMerchant ? 1f : 0f;

        out[12] = in.mccRisk;
        out[13] = clamp(in.merchantAvg * INV_MAX_MERCHANT_AVG);
    }

    private static float clamp(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}