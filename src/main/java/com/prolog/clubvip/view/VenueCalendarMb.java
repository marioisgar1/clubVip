package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.LocationExpenseVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * "Agenda de bazares" (CALENDAR_VIEW), the web version of the app's CalendarActivity: a month
 * calendar of where the seller is going to be each day, built from the location expenses
 * (locationExpense/getLocationExpenses). Read only; bookings are created in "Gastos de lugar".
 * The whole list is loaded once and cut per month here, like the app does.
 */
@Named("venueCalendarMb")
@ViewScoped
public class VenueCalendarMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<LocationExpenseVo>>() { }.getType();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** Same palette as the app (R.array.calendar_event_colors), picked by booking id. */
    private static final String[] PALETTE = {"#5B7720", "#2E7D8F", "#7B4B94", "#0E7A43", "#A11A2A", "#D4761A"};

    /** One booked spell at a venue. */
    public static class Booking implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer id;
        private Integer locationId;
        private String location;
        private String description;
        private LocalDate start;
        private LocalDate end;
        private String color;

        public Integer getId() { return id; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public LocalDate getStart() { return start; }
        public LocalDate getEnd() { return end; }
        public String getColor() { return color; }
        public int getDays() { return (int) ChronoUnit.DAYS.between(start, end) + 1; }
        /** What the seller called it, or the plain location. */
        public String getTitle() {
            return description != null && !description.isBlank() ? description.trim() : location;
        }
        boolean covers(LocalDate d) { return !d.isBefore(start) && !d.isAfter(end); }
    }

    /** One square of the month grid. */
    public static class Cell implements Serializable {
        private static final long serialVersionUID = 1L;
        private final LocalDate date;
        private final boolean inMonth;
        private final List<Booking> bookings = new ArrayList<>();
        private boolean today;
        private boolean selected;

        Cell(LocalDate date, boolean inMonth) {
            this.date = date;
            this.inMonth = inMonth;
        }

        public LocalDate getDate() { return date; }
        public int getDay() { return date.getDayOfMonth(); }
        public boolean isInMonth() { return inMonth; }
        public boolean isToday() { return today; }
        public boolean isSelected() { return selected; }
        public List<Booking> getBookings() { return bookings; }
        public boolean isBusy() { return !bookings.isEmpty(); }
        /** At most two chips fit in a cell; the rest is "+N". */
        public List<Booking> getShown() { return bookings.size() <= 2 ? bookings : bookings.subList(0, 2); }
        public int getMore() { return Math.max(0, bookings.size() - 2); }
    }

    private final List<Booking> all = new ArrayList<>();
    private YearMonth month;
    private LocalDate selected;
    private List<List<Cell>> weeks = new ArrayList<>();
    private List<Booking> monthBookings = new ArrayList<>();
    private int workingDays;

    @PostConstruct
    public void init() {
        month = YearMonth.from(today());
        refresh();
    }

    @Override
    public void refresh() {
        all.clear();
        try {
            List<LocationExpenseVo> list = RestClient.get("locationExpense/getLocationExpenses", LIST_TYPE, userId(), locale());
            if (list != null) {
                for (LocationExpenseVo vo : list) {
                    Booking b = toBooking(vo);
                    if (b != null) {
                        all.add(b);
                    }
                }
            }
        } catch (RestException e) {
            errors(e);
        }
        all.sort(Comparator.comparing((Booking b) -> b.start).thenComparing(b -> b.end));
        build();
    }

    private Booking toBooking(LocationExpenseVo vo) {
        try {
            Booking b = new Booking();
            b.id = vo.getId();
            b.locationId = vo.getSaleLocationId();
            b.location = vo.getSaleLocationName();
            b.description = vo.getDescription();
            b.start = LocalDate.parse(vo.getStartDate().substring(0, 10), ISO);
            b.end = LocalDate.parse(vo.getEndDate().substring(0, 10), ISO);
            if (b.end.isBefore(b.start)) {
                return null;
            }
            // One colour per bazaar (same rule as the app's EventColors.forBooking).
            Integer key = b.id != null ? b.id : b.locationId;
            b.color = PALETTE[key != null ? Math.abs(key) % PALETTE.length : 0];
            return b;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Whole weeks around the month, each day with the bookings that cover it. */
    private void build() {
        LocalDate today = today();
        DayOfWeek firstDow = WeekFields.of(locale()).getFirstDayOfWeek();
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();
        LocalDate gridStart = first.minusDays((first.getDayOfWeek().getValue() - firstDow.getValue() + 7) % 7);
        LocalDate gridEnd = last.plusDays((firstDow.getValue() + 6 - last.getDayOfWeek().getValue() + 7) % 7);

        weeks = new ArrayList<>();
        List<Cell> week = null;
        workingDays = 0;
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
            if (week == null || week.size() == 7) {
                week = new ArrayList<>();
                weeks.add(week);
            }
            Cell c = new Cell(d, YearMonth.from(d).equals(month));
            c.today = d.equals(today);
            c.selected = d.equals(selected);
            for (Booking b : all) {
                if (b.covers(d)) {
                    c.bookings.add(b);
                }
            }
            if (c.inMonth && c.isBusy()) {
                workingDays++;
            }
            week.add(c);
        }
        monthBookings = new ArrayList<>();
        for (Booking b : all) {
            if (!b.end.isBefore(first) && !b.start.isAfter(last)) {
                monthBookings.add(b);
            }
        }
    }

    // ------------------------------------------------------------------ actions

    public void prevMonth() {
        month = month.minusMonths(1);
        selected = null;
        build();
    }

    public void nextMonth() {
        month = month.plusMonths(1);
        selected = null;
        build();
    }

    public void goToday() {
        month = YearMonth.from(today());
        selected = today();
        build();
    }

    /** Tapping the selected day again goes back to the whole month (as in the app). */
    public void selectDay(LocalDate day) {
        selected = day == null || day.equals(selected) ? null : day;
        if (selected != null && !YearMonth.from(selected).equals(month)) {
            month = YearMonth.from(selected);
        }
        build();
    }

    public void wholeMonth() {
        selected = null;
        build();
    }

    // ------------------------------------------------------------------ view data

    public List<Booking> getVisible() {
        if (selected == null) {
            return monthBookings;
        }
        List<Booking> list = new ArrayList<>();
        for (Booking b : all) {
            if (b.covers(selected)) {
                list.add(b);
            }
        }
        return list;
    }

    public List<String> getWeekdays() {
        DayOfWeek firstDow = WeekFields.of(locale()).getFirstDayOfWeek();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            names.add(capitalize(firstDow.plus(i).getDisplayName(TextStyle.SHORT, locale()).replace(".", "")));
        }
        return names;
    }

    public String getMonthTitle() {
        return capitalize(month.getMonth().getDisplayName(TextStyle.FULL, locale())) + " " + month.getYear();
    }

    public String getMonthName() {
        return month.getMonth().getDisplayName(TextStyle.FULL, locale());
    }

    public String getSelectionTitle() {
        if (selected == null) {
            return sessionMb.text("vcal.list.month").replace("{0}", getMonthName());
        }
        return capitalize(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale()).format(selected));
    }

    public boolean isDaySelected() {
        return selected != null;
    }

    public boolean isCurrentMonth() {
        return month.equals(YearMonth.from(today()));
    }

    /** Bookings that cover today. */
    public List<Booking> getTodayBookings() {
        List<Booking> list = new ArrayList<>();
        for (Booking b : all) {
            if (b.covers(today())) {
                list.add(b);
            }
        }
        return list;
    }

    /** The next booking that has not started yet. */
    public Booking getNextBooking() {
        LocalDate today = today();
        for (Booking b : all) {
            if (b.start.isAfter(today)) {
                return b;
            }
        }
        return null;
    }

    public String range(Booking b) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE d MMM", locale());
        return b.start.equals(b.end) ? f.format(b.start) : f.format(b.start) + " → " + f.format(b.end);
    }

    /** "Hoy", "Mañana", "En 5 días", "Terminó" or "Día 2 de 3". */
    public String status(Booking b) {
        LocalDate today = today();
        if (b.end.isBefore(today)) {
            return sessionMb.text("vcal.status.past");
        }
        if (b.covers(today)) {
            if (b.getDays() == 1) {
                return sessionMb.text("vcal.status.today");
            }
            return sessionMb.text("vcal.status.dayOf")
                    .replace("{0}", String.valueOf(ChronoUnit.DAYS.between(b.start, today) + 1))
                    .replace("{1}", String.valueOf(b.getDays()));
        }
        long in = ChronoUnit.DAYS.between(today, b.start);
        return in == 1 ? sessionMb.text("vcal.status.tomorrow")
                : sessionMb.text("vcal.status.in").replace("{0}", String.valueOf(in));
    }

    /** CSS modifier of the status badge: now, soon (within a week), later, past. */
    public String statusClass(Booking b) {
        LocalDate today = today();
        if (b.end.isBefore(today)) return "past";
        if (b.covers(today)) return "now";
        return ChronoUnit.DAYS.between(today, b.start) <= 7 ? "soon" : "later";
    }

    public String daysText(int days) {
        return days + " " + sessionMb.text(days == 1 ? "pos.report.day" : "pos.report.days");
    }

    public List<List<Cell>> getWeeks() {
        return weeks;
    }

    public List<Booking> getMonthBookings() {
        return monthBookings;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public int getMonthDays() {
        return month.lengthOfMonth();
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase(locale()) + s.substring(1);
    }
}
