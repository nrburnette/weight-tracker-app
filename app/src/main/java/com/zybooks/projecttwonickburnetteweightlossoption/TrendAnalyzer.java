package com.zybooks.projecttwonickburnetteweightlossoption;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class TrendAnalyzer {

    private static final long MS_PER_DAY = 86_400_000L;
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private TrendAnalyzer() {} // utility constructor only

    // algorithm for analyzing the trends
    public static TrendSummary analyze(List<WeightEntry> entries) {
        TrendSummary s = new TrendSummary();

        if (entries == null || entries.size() < 2) {
            s.hasEnoughData = false;    // check for enough data to analyze
            s.totalChange = 0f;     // total change from start to now
            s.totalChangePct = 0f;  // percentage of total change
            s.weeklyPace = 0f;  // weekly change trend
            return s;
        }

        s.hasEnoughData = true;

        // gather entries from db, should already be in order
        float first = parseWeight(entries.get(0).getWeight());
        float last  = parseWeight(entries.get(entries.size() - 1).getWeight());

        s.totalChange = last - first;
        s.totalChangePct = (first != 0f) ? (s.totalChange / first) * 100f : 0f;


        // Ensure dates ordered oldest to newest :
        java.util.Collections.sort(entries, (a, b) ->
                parseDate(a.getDate()).compareTo(parseDate(b.getDate())));

        // reference on the last entry
        int lastIdx = entries.size() - 1;
        Date lastDate    = parseDate(entries.get(lastIdx).getDate());
        float lastWeight = parseWeight(entries.get(lastIdx).getWeight());

        // lastDate - 28 days
        final long MS_PER_DAY = 86_400_000L;
        Date cutoff = new Date(lastDate.getTime() - 28L * MS_PER_DAY);

        // get the FIRST entry with date is >= cutoff
        WeightEntry oldestInWindow = null;
        for (int i = 0; i < entries.size(); i++) {
            Date d = parseDate(entries.get(i).getDate());
            if (!d.before(cutoff)) {           // d >= cutoff
                oldestInWindow = entries.get(i);
                break;                          // this is the oldest inside the window
            }
        }

        // Compute weekly pace only if there's an older point than the last
        if (oldestInWindow != null && oldestInWindow != entries.get(lastIdx)) {
            float oldWeight = parseWeight(oldestInWindow.getWeight());
            Date  oldDate   = parseDate(oldestInWindow.getDate());
            float days      = daysBetween(oldDate, lastDate);
            float delta     = lastWeight - oldWeight;
            s.weeklyPace    = (days > 0f) ? (delta / (days / 7f)) : 0f;
        } else {
            s.weeklyPace = 0f; // not enough to compute a pace
        }

        return s;
    }

    // parsing helpers for strings -> numbers on weights, dates, and days

    private static float parseWeight(String s) {
        try { return Float.parseFloat(s); }
        catch (NumberFormatException e) { return 0f; }
    }

    private static Date parseDate(String s) {
        try {
            return SDF.parse(s);
        } catch (java.text.ParseException e) {
            return new Date(0);  // catch for if parsing fails
        }
    }

    private static float daysBetween(Date start, Date end) {
        long ms = end.getTime() - start.getTime();
        return (ms <= 0) ? 0f : (ms / (float) MS_PER_DAY);
    }


}
