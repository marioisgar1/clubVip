package com.prolog.clubvip.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.prolog.clubvip.vo.AppointmentVo;

/**
 * One row of the agenda: a visit (every service the customer booked together, {@code visitId})
 * or a single appointment. The agenda, the day list and the detail dialog work on visits so the
 * business starts, closes and charges all the services of a customer in one step.
 */
public class VisitRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<AppointmentVo> appointments = new ArrayList<>();

    private VisitRow() {
    }

    /** Groups appointments by visit (standalone ones stay alone), ordered by date and start time. */
    public static List<VisitRow> group(List<AppointmentVo> list) {
        Map<String, VisitRow> rows = new LinkedHashMap<>();
        if (list != null) {
            for (AppointmentVo a : list) {
                if (a == null) {
                    continue;
                }
                String key = a.getVisitId() != null ? "v" + a.getVisitId() : "a" + a.getId();
                rows.computeIfAbsent(key, k -> new VisitRow()).appointments.add(a);
            }
        }
        List<VisitRow> result = new ArrayList<>(rows.values());
        for (VisitRow r : result) {
            r.appointments.sort(Comparator.comparing(VisitRow::sortKey));
        }
        result.sort(Comparator.comparing(r -> sortKey(r.getPrimary())));
        return result;
    }

    /** The visit of a detailed appointment: its siblings when it has any, else just itself. */
    public static VisitRow of(AppointmentVo detail) {
        VisitRow r = new VisitRow();
        if (detail != null) {
            if (detail.getVisitAppointments() != null && !detail.getVisitAppointments().isEmpty()) {
                r.appointments.addAll(detail.getVisitAppointments());
            } else {
                r.appointments.add(detail);
            }
            r.appointments.sort(Comparator.comparing(VisitRow::sortKey));
        }
        return r;
    }

    private static String sortKey(AppointmentVo a) {
        return String.valueOf(a.getDate()) + " " + String.valueOf(a.getStartTime()) + " " + a.getId();
    }

    // ------------------------------------------------------------------ identity

    /** First appointment of the visit: the one the actions are sent to. */
    public AppointmentVo getPrimary() {
        return appointments.isEmpty() ? null : appointments.get(0);
    }

    public Integer getId() {
        return getPrimary() != null ? getPrimary().getId() : null;
    }

    public List<AppointmentVo> getAppointments() {
        return appointments;
    }

    public int getCount() {
        return appointments.size();
    }

    public boolean isMulti() {
        return appointments.size() > 1;
    }

    // ------------------------------------------------------------------ summary

    public String getCustomerName() {
        return getPrimary() != null ? getPrimary().getCustomerName() : null;
    }

    public String getCustomerPhone() {
        return getPrimary() != null ? getPrimary().getCustomerPhone() : null;
    }

    public Integer getEmployeeId() {
        return getPrimary() != null ? getPrimary().getEmployeeId() : null;
    }

    public String getEmployeeName() {
        return getPrimary() != null ? getPrimary().getEmployeeName() : null;
    }

    public String getDate() {
        return getPrimary() != null ? getPrimary().getDate() : null;
    }

    public String getStartTime() {
        return getPrimary() != null ? getPrimary().getStartTime() : null;
    }

    /** End of the last service of the visit. */
    public String getEndTime() {
        String end = null;
        for (AppointmentVo a : appointments) {
            if (a.getEndTime() != null && (end == null || a.getEndTime().compareTo(end) > 0)) {
                end = a.getEndTime();
            }
        }
        return end;
    }

    /** "Gel Semipermanente + Pestañas clásicas" (cancelled services left out when others remain). */
    public String getServiceNames() {
        StringBuilder sb = new StringBuilder();
        for (AppointmentVo a : active()) {
            sb.append(sb.length() > 0 ? " + " : "").append(a.getServiceName());
        }
        return sb.toString();
    }

    public int getDurationMinutes() {
        int total = 0;
        for (AppointmentVo a : active()) {
            total += a.getDurationMinutes() != null ? a.getDurationMinutes() : 0;
        }
        return total;
    }

    /** "1 h 30 min" / "45 min". */
    public String getDurationLabel() {
        int m = getDurationMinutes();
        if (m < 60) {
            return m + " min";
        }
        return (m / 60) + " h" + (m % 60 > 0 ? " " + (m % 60) + " min" : "");
    }

    /** Tips of the visit (for the staff; not part of getTotal()). */
    public BigDecimal getTipTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AppointmentVo a : appointments) {
            if (a.getTipAmount() != null) {
                sum = sum.add(a.getTipAmount());
            }
        }
        return sum;
    }

    public boolean isHasTip() {
        return getTipTotal().signum() > 0;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AppointmentVo a : active()) {
            if (a.getTotal() != null) {
                sum = sum.add(a.getTotal());
            }
        }
        return sum;
    }

    public int getItemsCount() {
        int n = 0;
        for (AppointmentVo a : appointments) {
            n += a.getItems() != null ? a.getItems().size() : 0;
        }
        return n;
    }

    /**
     * Status of the visit: in progress when any service is, booked when any still waits, done when
     * every performed service closed, else no-show / cancelled.
     */
    public String getStatus() {
        boolean inProgress = false, booked = false, done = false, noShow = false;
        for (AppointmentVo a : appointments) {
            String s = a.getAppointmentStatus();
            inProgress |= "IN_PROGRESS".equals(s);
            booked |= "BOOKED".equals(s);
            done |= "DONE".equals(s);
            noShow |= "NO_SHOW".equals(s);
        }
        if (inProgress) {
            return "IN_PROGRESS";
        }
        if (booked) {
            return "BOOKED";
        }
        if (done) {
            return "DONE";
        }
        return noShow ? "NO_SHOW" : "CANCELLED";
    }

    public boolean isAnyBooked() {
        return has("BOOKED");
    }

    public boolean isAnyOpen() {
        return has("BOOKED") || has("IN_PROGRESS");
    }

    public boolean isAllWaiting() {
        return isAnyBooked() && !has("IN_PROGRESS") && !has("DONE");
    }

    private boolean has(String status) {
        for (AppointmentVo a : appointments) {
            if (status.equals(a.getAppointmentStatus())) {
                return true;
            }
        }
        return false;
    }

    /** Services that count for names, duration and totals: all but the cancelled / no-show ones. */
    private List<AppointmentVo> active() {
        List<AppointmentVo> result = new ArrayList<>();
        for (AppointmentVo a : appointments) {
            if (!"CANCELLED".equals(a.getAppointmentStatus()) && !"NO_SHOW".equals(a.getAppointmentStatus())) {
                result.add(a);
            }
        }
        return result.isEmpty() ? appointments : result;
    }

    /** First open appointment (the one to close), or the primary. */
    public AppointmentVo getFirstOpen() {
        for (AppointmentVo a : appointments) {
            if ("BOOKED".equals(a.getAppointmentStatus()) || "IN_PROGRESS".equals(a.getAppointmentStatus())) {
                return a;
            }
        }
        return getPrimary();
    }
}
