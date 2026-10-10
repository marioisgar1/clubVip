package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolog.clubvip.vo.DayHoursVo;

/**
 * Editable weekly schedule shown by /WEB-INF/includes/weekly-hours.xhtml: always the 7 weekdays
 * (Monday first), each open or closed with its hours and, for businesses, an optional break.
 */
public class WeekModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<DayHoursVo> days = new ArrayList<>();

    /** Copy of {@code source} completed to 7 days; days missing are closed. */
    public static WeekModel of(List<DayHoursVo> source) {
        WeekModel m = new WeekModel();
        Map<Integer, DayHoursVo> byDay = new HashMap<>();
        if (source != null) {
            for (DayHoursVo d : source) {
                if (d != null && d.getWeekday() != null) {
                    byDay.put(d.getWeekday(), d);
                }
            }
        }
        for (int w = 1; w <= 7; w++) {
            DayHoursVo src = byDay.get(w);
            DayHoursVo d = new DayHoursVo(w, src != null && Boolean.TRUE.equals(src.getOpen()),
                    src != null ? src.getStartTime() : null, src != null ? src.getEndTime() : null);
            if (src != null) {
                d.setBreakStartTime(src.getBreakStartTime());
                d.setBreakEndTime(src.getBreakEndTime());
            }
            m.days.add(d);
        }
        return m;
    }

    /** Monday to Saturday {@code start}-{@code end}, Sunday closed (new businesses). */
    public static WeekModel standard(String start, String end) {
        List<DayHoursVo> list = new ArrayList<>();
        for (int w = 1; w <= 6; w++) {
            list.add(new DayHoursVo(w, true, start, end));
        }
        return of(list);
    }

    public List<DayHoursVo> getDays() {
        return days;
    }

    /** A day switched open gets the hours of another open day (or 09:00-18:00). */
    public void toggled(DayHoursVo day) {
        if (day == null || !Boolean.TRUE.equals(day.getOpen())) {
            return;
        }
        if (isBlank(day.getStartTime()) || isBlank(day.getEndTime())) {
            DayHoursVo ref = firstOpenOtherThan(day);
            day.setStartTime(ref != null ? ref.getStartTime() : "09:00");
            day.setEndTime(ref != null ? ref.getEndTime() : "18:00");
            if (ref != null) {
                day.setBreakStartTime(ref.getBreakStartTime());
                day.setBreakEndTime(ref.getBreakEndTime());
            }
        }
    }

    /** Copies the hours of the first open day to every other open day. */
    public void copyToAll() {
        DayHoursVo ref = firstOpenOtherThan(null);
        if (ref == null) {
            return;
        }
        for (DayHoursVo d : days) {
            if (d != ref && Boolean.TRUE.equals(d.getOpen())) {
                d.setStartTime(ref.getStartTime());
                d.setEndTime(ref.getEndTime());
                d.setBreakStartTime(ref.getBreakStartTime());
                d.setBreakEndTime(ref.getBreakEndTime());
            }
        }
    }

    /** "Lun–Vie 9:00–18:00 · Sáb 8:00–16:00 · Dom cerrado" ({@code names}: 7 short day names). */
    public String describe(String[] names, String closedWord) {
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < 7) {
            String label = label(days.get(i), closedWord);
            int j = i;
            while (j + 1 < 7 && label(days.get(j + 1), closedWord).equals(label)) {
                j++;
            }
            parts.add((i == j ? names[i] : names[i] + "–" + names[j]) + " " + label);
            i = j + 1;
        }
        return String.join(" · ", parts);
    }

    private static String label(DayHoursVo d, String closedWord) {
        if (!Boolean.TRUE.equals(d.getOpen())) {
            return closedWord;
        }
        return trim(d.getStartTime()) + "–" + trim(d.getEndTime());
    }

    private static String trim(String hhmm) {
        return hhmm != null && hhmm.startsWith("0") ? hhmm.substring(1) : String.valueOf(hhmm);
    }

    private DayHoursVo firstOpenOtherThan(DayHoursVo exclude) {
        for (DayHoursVo d : days) {
            if (d != exclude && Boolean.TRUE.equals(d.getOpen()) && !isBlank(d.getStartTime()) && !isBlank(d.getEndTime())) {
                return d;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || s.contains("_");
    }
}
