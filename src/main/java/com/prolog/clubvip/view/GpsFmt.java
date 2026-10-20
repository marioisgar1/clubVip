package com.prolog.clubvip.view;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Dates of the GPS service: Instants ("2026-10-19T15:47:16.32Z") and the device time
 * ("2026-10-19T13:26:53", UTC without zone), shown in the zone the viewer chose
 * ({@link GpsZoneMb}, by default the one of their computer).
 */
public final class GpsFmt {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter HOUR_MIN = DateTimeFormatter.ofPattern("HH:mm");

    private GpsFmt() {
    }

    /** Instant or UTC local date-time text, or null. */
    public static Instant parse(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return iso.endsWith("Z") || iso.contains("+") ? Instant.parse(iso)
                    : LocalDateTime.parse(iso).toInstant(ZoneOffset.UTC);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** dd/MM/yyyy HH:mm:ss in {@code zone}; "—" when empty. */
    public static String dateTime(String iso, ZoneId zone) {
        Instant i = parse(iso);
        return i != null ? DATE_TIME.format(i.atZone(zone)) : "—";
    }

    /** HH:mm in {@code zone} (chart labels). */
    public static String hourMin(String iso, ZoneId zone) {
        Instant i = parse(iso);
        return i != null ? HOUR_MIN.format(i.atZone(zone)) : "";
    }

    /** 45 s, 12 min, 3 h 5 min, 2 d 4 h. */
    public static String duration(Long seconds) {
        if (seconds == null) return "—";
        long s = Math.max(0, seconds);
        if (s < 60) return s + " s";
        long min = s / 60;
        if (min < 60) return min + " min";
        long h = min / 60;
        if (h < 24) return h + " h" + (min % 60 > 0 ? " " + (min % 60) + " min" : "");
        long d = h / 24;
        return d + " d" + (h % 24 > 0 ? " " + (h % 24) + " h" : "");
    }
}
