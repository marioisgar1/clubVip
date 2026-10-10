package com.prolog.clubvip.view;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.primefaces.PrimeFaces;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 * Shared plumbing for the operation page beans: session access, growl helpers, status pills
 * and the "open a link in a new tab" trick used by every WhatsApp button.
 */
public abstract class BaseMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    protected SessionMb sessionMb;

    protected Integer userId() {
        return sessionMb.getUserId();
    }

    protected Locale locale() {
        return sessionMb.getLocale();
    }

    protected Integer businessId() {
        return sessionMb.getUser() != null ? sessionMb.getUser().getBusinessId() : null;
    }

    protected LocalDate today() {
        return LocalDate.now(ZoneId.of(Constants.TIMEZONE));
    }

    /** Growl (info) with an i18n key. */
    protected void info(String key) {
        addMessage(FacesMessage.SEVERITY_INFO, sessionMb.text(key));
    }

    /** Growl (error) with a raw, already translated text. */
    protected void error(String text) {
        addMessage(FacesMessage.SEVERITY_ERROR, text);
    }

    /** Growl (error) with an i18n key. */
    protected void errorKey(String key) {
        addMessage(FacesMessage.SEVERITY_ERROR, sessionMb.text(key));
    }

    /** Every backend message of a failed call becomes one growl entry. */
    protected void errors(RestException e) {
        if (e.getMessages() == null || e.getMessages().isEmpty()) {
            error(sessionMb.text("msg.error"));
            return;
        }
        for (String m : e.getMessages()) {
            error(m);
        }
    }

    protected void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, text, null));
        }
    }

    /** Opens the given URL in a new browser tab from an ajax response. */
    protected void openInNewTab(String link) {
        if (link == null || link.isBlank()) {
            return;
        }
        PrimeFaces.current().executeScript("window.open('" + jsEscape(link) + "','_blank')");
    }

    protected static String jsEscape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("'", "\\'");
    }

    /** CSS pill for an appointment status. */
    public String statusBadge(String status) {
        if (status == null) {
            return "badge badge-muted";
        }
        switch (status) {
            case "DONE":
                return "badge badge-success";
            case "IN_PROGRESS":
                return "badge badge-info";
            case "BOOKED":
                return "badge badge-warning";
            case "CANCELLED":
            case "NO_SHOW":
                return "badge badge-danger";
            default:
                return "badge badge-muted";
        }
    }

    protected static int nz(Integer v) {
        return v != null ? v : 0;
    }

    // ------------------------------------------------------------------ date helpers (POS)

    /**
     * Plain-day fields (due dates, first due date) travel as {@code yyyy-MM-dd}; Gson parses them
     * as midnight in the JVM default zone, so they are rendered back in that same zone.
     */
    public String fmtDay(Date day) {
        if (day == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(day);
    }

    /** Timestamps (sale date, payment date) are instants: shown in the business time zone. */
    public String fmtStamp(Date stamp) {
        return fmtInZone(stamp, "dd/MM/yyyy HH:mm");
    }

    /** Timestamp shown as a day only, in the business time zone. */
    public String fmtStampDay(Date stamp) {
        return fmtInZone(stamp, "dd/MM/yyyy");
    }

    private static String fmtInZone(Date stamp, String pattern) {
        if (stamp == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone(Constants.TIMEZONE));
        return df.format(stamp);
    }

    /** {@code yyyy-MM-dd} of a date in the JVM default zone (query parameters of plain days). */
    protected static String isoDay(Date day) {
        return day != null ? new SimpleDateFormat("yyyy-MM-dd").format(day) : "";
    }

    /** Midnight (JVM default zone) of a LocalDate, for p:datePicker fields bound to Date. */
    protected static Date toDate(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Digits-only phone, prefixed with the Mexico country code when it is a 10-digit local number. */
    protected static String waDigits(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            digits = "52" + digits;
        }
        return digits;
    }
}
