package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.EmployeeVo;

import jakarta.annotation.PostConstruct;

/**
 * Day list of appointments with date / employee / status filters. Shared by the business
 * "appointments of the day" page and the employee's "my appointments" page (the backend
 * already restricts employees to their own agenda).
 */
public abstract class AbstractAppointmentListMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type APPOINTMENT_LIST = new TypeToken<List<AppointmentVo>>() { }.getType();
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();

    private LocalDate date;
    private Integer employeeId;
    private String status;
    private boolean upcoming;
    private List<AppointmentVo> appointments = new ArrayList<>();
    private List<EmployeeVo> employees = new ArrayList<>();

    /** Whether the employee filter applies (false on the employee's own page). */
    protected abstract boolean employeeFilter();

    /** Whether the page opens listing every appointment from today on instead of a single day. */
    protected boolean upcomingByDefault() {
        return false;
    }

    @PostConstruct
    public void init() {
        date = today();
        upcoming = upcomingByDefault();
        if (employeeFilter()) {
            try {
                List<EmployeeVo> list = RestClient.get("employee/getEmployees", EMPLOYEE_LIST, userId(), locale());
                employees = list != null ? list : new ArrayList<>();
            } catch (RestException e) {
                errors(e);
            }
        }
        refresh();
    }

    @Override
    public void refresh() {
        List<String> params = new ArrayList<>();
        if (!upcoming) {
            params.add("date=" + (date != null ? date : today()));
        }
        if (employeeFilter() && employeeId != null) {
            params.add("employeeId=" + employeeId);
        }
        if (status != null && !status.isBlank()) {
            params.add("status=" + status);
        }
        String path = (upcoming ? "appointment/getUpcoming" : "appointment/getAppointments")
                + (params.isEmpty() ? "" : "?" + String.join("&", params));
        try {
            List<AppointmentVo> list = RestClient.get(path, APPOINTMENT_LIST, userId(), locale());
            appointments = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            appointments = new ArrayList<>();
            errors(e);
        }
    }

    /** Switches to the list of every appointment from today on. */
    public void showUpcoming() {
        upcoming = true;
        refresh();
    }

    /** Back to the single-day list (fired by the date picker). */
    public void selectDay() {
        upcoming = false;
        refresh();
    }

    /** Shows {@code day} (where a visit was just started / charged), unless listing every upcoming one. */
    public void showDay(LocalDate day) {
        if (!upcoming && day != null) {
            date = day;
        }
        refresh();
    }

    public void previousDay() {
        upcoming = false;
        date = (date != null ? date : today()).minusDays(1);
        refresh();
    }

    public void nextDay() {
        upcoming = false;
        date = (date != null ? date : today()).plusDays(1);
        refresh();
    }

    public void goToday() {
        upcoming = false;
        date = today();
        refresh();
    }

    /** The list grouped by visit: one row per customer visit (services booked together). */
    public List<VisitRow> getVisits() {
        return VisitRow.group(appointments);
    }

    public int productsCount(AppointmentVo a) {
        return a != null && a.getItems() != null ? a.getItems().size() : 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AppointmentVo a : appointments) {
            if ("DONE".equals(a.getAppointmentStatus()) && a.getTotal() != null) {
                sum = sum.add(a.getTotal());
            }
        }
        return sum;
    }

    public String getDateLabel() {
        if (upcoming) {
            return sessionMb.text("op.my.upcomingLabel");
        }
        return sessionMb.fmtDay(String.valueOf(date != null ? date : today()));
    }

    public boolean isToday() {
        return !upcoming && today().equals(date);
    }

    public boolean isUpcoming() {
        return upcoming;
    }

    /** Only pages that open in "upcoming" mode offer the button to come back to it. */
    public boolean isUpcomingAvailable() {
        return upcomingByDefault();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AppointmentVo> getAppointments() {
        return appointments;
    }

    public List<EmployeeVo> getEmployees() {
        return employees;
    }
}
