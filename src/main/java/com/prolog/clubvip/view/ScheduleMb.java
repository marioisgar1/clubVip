package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.BusinessVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.OccupancyVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Weekly agenda: business days x opening hours grid with the appointments starting in each
 * hour, employee filter/legend, and (admins) the occupancy bars of the week.
 */
@Named("scheduleMb")
@ViewScoped
public class ScheduleMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type APPOINTMENT_LIST = new TypeToken<List<AppointmentVo>>() { }.getType();
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();
    private static final Type OCCUPANCY_LIST = new TypeToken<List<OccupancyVo>>() { }.getType();
    private static final String[] PALETTE = {"#5B7720", "#2F5FA8", "#0E7A43", "#A11A2A", "#6A3FA0",
        "#C2651C", "#1B8A8F", "#7A5C1E"};

    private LocalDate weekStart;
    private BusinessVo business;
    private List<EmployeeVo> employees = new ArrayList<>();
    private Integer employeeId;
    private List<AppointmentVo> appointments = new ArrayList<>();
    private List<OccupancyVo> occupancy = new ArrayList<>();

    private List<LocalDate> days = new ArrayList<>();
    private List<Integer> hours = new ArrayList<>();
    private Set<Integer> closedWeekdays = new HashSet<>();
    private Map<String, List<VisitRow>> cells = new HashMap<>();

    /** Height of one hour row in px (slot 64 + row gap 4): blocks are placed and sized in minutes with it. */
    private static final int HOUR_PX = 68;

    /** Side-by-side lane of each visit block and the lanes of its group of overlapping visits ([lane, lanes]). */
    private Map<Integer, int[]> lanes = new HashMap<>();
    private Map<Integer, String> colors = new HashMap<>();

    @PostConstruct
    public void init() {
        weekStart = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        loadBusiness();
        loadEmployees();
        refresh();
    }

    private void loadBusiness() {
        try {
            business = RestClient.get("business/getMyBusiness", BusinessVo.class, userId(), locale());
        } catch (RestException e) {
            errors(e);
        }
        if (business == null) {
            business = new BusinessVo();
            errorKey("op.schedule.noBusiness");
        }
        closedWeekdays = new HashSet<>();
        if (business.getClosedWeekdays() != null) {
            for (String part : business.getClosedWeekdays().split("[^0-9]+")) {
                if (!part.isEmpty()) {
                    closedWeekdays.add(Integer.valueOf(part));
                }
            }
        }
        int open = hourOf(business.getOpeningTime(), 9, false);
        int close = hourOf(business.getClosingTime(), 19, true);
        if (close <= open) {
            close = open + 10;
        }
        hours = new ArrayList<>();
        for (int h = open; h < close; h++) {
            hours.add(h);
        }
    }

    private void loadEmployees() {
        try {
            List<EmployeeVo> list = RestClient.get("employee/getEmployees", EMPLOYEE_LIST, userId(), locale());
            employees = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            employees = new ArrayList<>();
            errors(e);
        }
        colors = new HashMap<>();
        int i = 0;
        for (EmployeeVo e : employees) {
            colors.put(e.getId(), PALETTE[i % PALETTE.length]);
            i++;
        }
    }

    @Override
    public void refresh() {
        days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            if (!closedWeekdays.contains(d.getDayOfWeek().getValue())) {
                days.add(d);
            }
        }
        if (days.isEmpty()) {
            days.add(weekStart);
        }
        try {
            List<AppointmentVo> list = RestClient.get("appointment/getWeek?from=" + weekStart, APPOINTMENT_LIST,
                    userId(), locale());
            appointments = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            appointments = new ArrayList<>();
            errors(e);
        }
        buildCells();
        occupancy = new ArrayList<>();
        if (sessionMb.isAdmin()) {
            try {
                List<OccupancyVo> list = RestClient.get("report/occupancy?from=" + weekStart, OCCUPANCY_LIST,
                        userId(), locale());
                occupancy = list != null ? list : new ArrayList<>();
            } catch (RestException e) {
                errors(e);
            }
        }
    }

    private void buildCells() {
        cells = new HashMap<>();
        List<AppointmentVo> shown = new ArrayList<>();
        for (AppointmentVo a : appointments) {
            if ("CANCELLED".equals(a.getAppointmentStatus())) {
                continue;
            }
            if (employeeId != null && !employeeId.equals(a.getEmployeeId())) {
                continue;
            }
            if (a.getDate() == null || a.getStartTime() == null || a.getStartTime().length() < 2) {
                continue;
            }
            shown.add(a);
        }
        // A visit (several services booked together) is one chip, placed at the hour it starts.
        for (VisitRow v : VisitRow.group(shown)) {
            String key = v.getDate() + "|" + Integer.parseInt(v.getStartTime().substring(0, 2));
            cells.computeIfAbsent(key, k -> new ArrayList<>()).add(v);
        }
        for (List<VisitRow> list : cells.values()) {
            list.sort((x, y) -> String.valueOf(x.getStartTime()).compareTo(String.valueOf(y.getStartTime())));
        }
        // Overlapping visits of a day sit side by side, like a calendar: greedy lanes per overlap group.
        lanes = new HashMap<>();
        Map<String, List<VisitRow>> byDay = new HashMap<>();
        for (VisitRow v : VisitRow.group(shown)) {
            byDay.computeIfAbsent(v.getDate(), k -> new ArrayList<>()).add(v);
        }
        for (List<VisitRow> day : byDay.values()) {
            day.sort((x, y) -> minutes(x.getStartTime()) - minutes(y.getStartTime()));
            List<VisitRow> group = new ArrayList<>();
            List<Integer> laneEnds = new ArrayList<>();
            int groupEnd = -1;
            for (VisitRow v : day) {
                int start = minutes(v.getStartTime());
                int end = Math.max(start + 15, minutes(v.getEndTime()));
                if (start >= groupEnd && !group.isEmpty()) {
                    closeGroup(group, laneEnds.size());
                    group = new ArrayList<>();
                    laneEnds = new ArrayList<>();
                }
                int lane = -1;
                for (int i = 0; i < laneEnds.size(); i++) {
                    if (laneEnds.get(i) <= start) {
                        lane = i;
                        break;
                    }
                }
                if (lane < 0) {
                    lane = laneEnds.size();
                    laneEnds.add(end);
                } else {
                    laneEnds.set(lane, end);
                }
                lanes.put(v.getId(), new int[] {lane, 1});
                group.add(v);
                groupEnd = Math.max(groupEnd, end);
            }
            closeGroup(group, laneEnds.size());
        }
    }

    private void closeGroup(List<VisitRow> group, int count) {
        for (VisitRow v : group) {
            lanes.get(v.getId())[1] = Math.max(1, count);
        }
    }

    private static int minutes(String hhmm) {
        if (hhmm == null || hhmm.length() < 4) {
            return 0;
        }
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + (p.length > 1 ? Integer.parseInt(p[1]) : 0);
    }

    /**
     * Position of a visit block inside the cell of the hour it starts: top by its minutes, height by the
     * whole visit (start to end) so it reaches the hour it finishes; side by side when visits overlap.
     */
    public String chipStyle(VisitRow v) {
        int start = minutes(v.getStartTime());
        int end = Math.max(start + 15, minutes(v.getEndTime()));
        int top = Math.round((start % 60) * HOUR_PX / 60f) + 2;
        int height = Math.max(26, Math.round((end - start) * HOUR_PX / 60f) - 4);
        int[] l = lanes.getOrDefault(v.getId(), new int[] {0, 1});
        double width = 100.0 / l[1];
        return String.format(java.util.Locale.ROOT,
                "top:%dpx;height:%dpx;left:calc(%.3f%% + 3px);width:calc(%.3f%% - 6px);", top, height, width * l[0], width);
    }

    /** Short blocks (under 45 min) only show the time and the services. */
    public boolean isCompact(VisitRow v) {
        return minutes(v.getEndTime()) - minutes(v.getStartTime()) < 45;
    }

    /** Employee filter changed: no backend call needed, only regroup. */
    public void onEmployeeChange() {
        buildCells();
    }

    public void previousWeek() {
        weekStart = weekStart.minusWeeks(1);
        refresh();
    }

    public void nextWeek() {
        weekStart = weekStart.plusWeeks(1);
        refresh();
    }

    public void thisWeek() {
        weekStart = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        refresh();
    }

    // ------------------------------------------------------------------ grid helpers

    public List<VisitRow> cell(LocalDate day, Integer hour) {
        List<VisitRow> list = cells.get(day + "|" + hour);
        return list != null ? list : new ArrayList<>();
    }

    public boolean isBreak(Integer hour) {
        if (business == null || business.getBreakStartTime() == null || business.getBreakEndTime() == null) {
            return false;
        }
        int from = hourOf(business.getBreakStartTime(), -1, false);
        int to = hourOf(business.getBreakEndTime(), -1, true);
        return from >= 0 && hour >= from && hour < to;
    }

    public boolean isToday(LocalDate day) {
        return today().equals(day);
    }

    public boolean isPast(LocalDate day) {
        return day.isBefore(today());
    }

    public String hourLabel(Integer hour) {
        return String.format("%02d:00", hour);
    }

    public String dayLabel(LocalDate day) {
        return sessionMb.text("weekday." + day.getDayOfWeek().getValue()) + " " + day.getDayOfMonth();
    }

    public String employeeColor(Integer id) {
        String c = colors.get(id);
        return c != null ? c : "#6E7278";
    }

    public String getWeekLabel() {
        return sessionMb.fmtDay(weekStart.toString()) + " - " + sessionMb.fmtDay(weekStart.plusDays(6).toString());
    }

    public String getGridColumns() {
        return "70px repeat(" + days.size() + ", minmax(110px, 1fr))";
    }

    public int occupancyWidth(OccupancyVo o) {
        if (o == null || o.getOccupancyPercent() == null) {
            return 0;
        }
        int p = o.getOccupancyPercent().intValue();
        return Math.max(0, Math.min(100, p));
    }

    public int getWeekCount() {
        int n = 0;
        for (AppointmentVo a : appointments) {
            if (!"CANCELLED".equals(a.getAppointmentStatus())) {
                n++;
            }
        }
        return n;
    }

    private static int hourOf(String hhmm, int fallback, boolean ceil) {
        if (hhmm == null || hhmm.length() < 2) {
            return fallback;
        }
        try {
            String[] p = hhmm.split(":");
            int h = Integer.parseInt(p[0].trim());
            int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
            return (ceil && m > 0) ? h + 1 : h;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public BusinessVo getBusiness() {
        return business;
    }

    public List<EmployeeVo> getEmployees() {
        return employees;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public List<AppointmentVo> getAppointments() {
        return appointments;
    }

    public List<OccupancyVo> getOccupancy() {
        return occupancy;
    }

    public List<LocalDate> getDays() {
        return days;
    }

    public List<Integer> getHours() {
        return hours;
    }
}
