package br.kauesoares;

import br.kauesoares.data.MccRiskDataset;
import br.kauesoares.dto.Input;
import br.kauesoares.dto.RequestDTO;

public class InputMapper {

    private final MccRiskDataset mccRisk;

    public InputMapper(MccRiskDataset mccRisk) {
        this.mccRisk = mccRisk;
    }

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

            out.minutesSince = diffMinutes(
                    r.lastTransaction.timestamp,
                    ts
            );

            out.kmLast = r.lastTransaction.kmFromCurrent;
        } else {
            out.hasLast = false;
            out.minutesSince = -1;
            out.kmLast = -1;
        }
    }

    private static int parseHour(String ts) {
        return (ts.charAt(11) - '0') * 10 + (ts.charAt(12) - '0');
    }

    private static int parseDayOfWeek(String ts) {
        int y = (ts.charAt(0)-'0')*1000 + (ts.charAt(1)-'0')*100 +
                (ts.charAt(2)-'0')*10   + (ts.charAt(3)-'0');

        int m = (ts.charAt(5)-'0')*10 + (ts.charAt(6)-'0');
        int d = (ts.charAt(8)-'0')*10 + (ts.charAt(9)-'0');

        int[] t = {0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4};

        if (m < 3) y -= 1;

        return (y + y/4 - y/100 + y/400 + t[m-1] + d + 6) % 7;
    }

    private static double diffMinutes(String a, String b) {
        return diffSeconds(a, b) / 60.0;
    }

    private static long diffSeconds(String a, String b) {
        return toSeconds(b) - toSeconds(a);
    }

    private static long toSeconds(String ts) {
        int y = (ts.charAt(0)-'0')*1000 + (ts.charAt(1)-'0')*100 +
                (ts.charAt(2)-'0')*10   + (ts.charAt(3)-'0');

        int m = (ts.charAt(5)-'0')*10 + (ts.charAt(6)-'0');
        int d = (ts.charAt(8)-'0')*10 + (ts.charAt(9)-'0');

        int hh = (ts.charAt(11)-'0')*10 + (ts.charAt(12)-'0');
        int mm = (ts.charAt(14)-'0')*10 + (ts.charAt(15)-'0');
        int ss = (ts.charAt(17)-'0')*10 + (ts.charAt(18)-'0');

        if (m < 3) {
            y--;
            m += 12;
        }

        long days = 365L*y + y/4 - y/100 + y/400
                + (153L*(m-3)+2)/5 + d - 1;

        return days * 86400L + hh * 3600L + mm * 60L + ss;
    }
}