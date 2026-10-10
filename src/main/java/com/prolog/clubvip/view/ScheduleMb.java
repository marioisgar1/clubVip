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
    private static final String[] PALETTE = {"#B8902F", "#2F5FA8", "#0E7A43", "#A11A2A", "#6A3FA0",
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
    private Map<String, List<AppointmentVo>> cells = new HashMap<>();
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
            String key = a.getDate() + "|" + Integer.parseInt(a.getStartTime().substring(0, 2));
            cells.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }
        for (List<AppointmentVo> list : cells.values()) {
            list.sort((x, y) -> String.valueOf(x.getStartTime()).compareTo(String.valueOf(y.getStartTime())));
        }
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

    public List<AppointmentVo> cell(LocalDate day, Integer hour) {
        List<AppointmentVo> list = cells.get(day + "|" + hour);
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
