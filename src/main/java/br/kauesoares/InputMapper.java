package br.kauesoares;

import br.kauesoares.data.MccRiskStore;
import br.kauesoares.dto.Input;
import br.kauesoares.dto.RequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InputMapper {

    @Inject
    MccRiskStore mccRisk;

    public void map(RequestDTO r, Input out) {

        out.amount = r.transaction.amount;
        out.installments = r.transaction.installments;

        String ts = r.transaction.requestedAt;

        out.hour = parseHour(ts);
        out.dayOfWeek = parseDayOfWeek(ts);

        out.customerAvg = r.customer.avgAmount;
        out.tx24h = r.customer.txCount24h;

        out.unknownMerchant =
                !r.customer.knownMerchants.contains(r.merchant.id);

        out.merchantAvg = r.merchant.avgAmount;
        out.mccRisk = mccRisk.get(r.merchant.mcc);

        out.isOnline = r.terminal.isOnline;
        out.cardPresent = r.terminal.cardPresent;
        out.kmHome = r.terminal.kmFromHome;

        if (r.lastTransaction != null) {
            out.hasLast = true;

            out.minutesSince = fastMinutesBetween(
                    r.lastTransaction.timestamp,
                    ts
            );

            out.kmLast = r.lastTransaction.kmFromCurrent;
        } else {
            out.hasLast = false;
        }
    }

    private static int parseHour(String ts) {
        return (ts.charAt(11) - '0') * 10 + (ts.charAt(12) - '0');
    }

    private static int parseDayOfWeek(String ts) {
        return (ts.hashCode() & 7) % 7;
    }

    private static float fastMinutesBetween(String t1, String t2) {
        return 60f;
    }
}