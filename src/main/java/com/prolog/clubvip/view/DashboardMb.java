package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.BusinessCustomerVo;
import com.prolog.clubvip.vo.CreditPortfolioVo;
import com.prolog.clubvip.vo.MessageComposeVo;
import com.prolog.clubvip.vo.SaleVo;
import com.prolog.clubvip.vo.TodaySummaryVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Business dashboard: today's KPIs, the day's appointments, birthdays and pending attention items.
 * Businesses without the appointments module (and sellers) get the SALES variant instead: today's
 * tickets and total from sale/getSales, and the credit portfolio from credit/getPortfolio.
 * A failing backend leaves an empty (but rendered) dashboard instead of an error page.
 */
@Named("dashboardMb")
@ViewScoped
public class DashboardMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionMb sessionMb;

    private static final Type SALE_LIST = new TypeToken<List<SaleVo>>() { }.getType();

    private TodaySummaryVo summary = new TodaySummaryVo();
    private boolean loaded;

    // --- sales variant ---
    private List<SaleVo> todaySales = new ArrayList<>();
    private CreditPortfolioVo portfolio = new CreditPortfolioVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    /** SALES variant: sellers, and businesses that switched the appointments module off. */
    public boolean isSalesVariant() {
        return sessionMb.isSeller() || !sessionMb.isAppointmentsEnabled();
    }

    public void refresh() {
        loaded = false;
        if (isSalesVariant()) {
            refreshSales();
            return;
        }
        try {
            TodaySummaryVo s = RestClient.get("appointment/getToday", TodaySummaryVo.class,
                    userId(), sessionMb.getLocale());
            summary = s != null ? s : new TodaySummaryVo();
            loaded = true;
        } catch (RestException e) {
            summary = new TodaySummaryVo();
            for (String m : e.getMessages()) {
                addMessage(FacesMessage.SEVERITY_WARN, m);
            }
        }
    }

    private void refreshSales() {
        String today = sessionMb.today();
        try {
            List<SaleVo> list = RestClient.get("sale/getSales?from=" + today + "&to=" + today, SALE_LIST,
                    userId(), sessionMb.getLocale());
            todaySales = list != null ? list : new ArrayList<>();
            loaded = true;
        } catch (RestException e) {
            todaySales = new ArrayList<>();
            for (String m : e.getMessages()) {
                addMessage(FacesMessage.SEVERITY_WARN, m);
            }
        }
        try {
            CreditPortfolioVo p = RestClient.get("credit/getPortfolio", CreditPortfolioVo.class, userId(), sessionMb.getLocale());
            portfolio = p != null ? p : new CreditPortfolioVo();
        } catch (RestException e) {
            portfolio = new CreditPortfolioVo();
        }
    }

    public int getTodaySalesCount() {
        int n = 0;
        for (SaleVo s : todaySales) {
            if (!"CANCELLED".equalsIgnoreCase(s.getSaleStatus())) {
                n++;
            }
        }
        return n;
    }

    public int getTodayCancelledCount() {
        int n = 0;
        for (SaleVo s : todaySales) {
            if ("CANCELLED".equalsIgnoreCase(s.getSaleStatus())) {
                n++;
            }
        }
        return n;
    }

    public BigDecimal getTodaySalesTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleVo s : todaySales) {
            if (!"CANCELLED".equalsIgnoreCase(s.getSaleStatus()) && s.getTotal() != null) {
                sum = sum.add(s.getTotal());
            }
        }
        return sum;
    }

    public int getTodayUnits() {
        int n = 0;
        for (SaleVo s : todaySales) {
            if (!"CANCELLED".equalsIgnoreCase(s.getSaleStatus()) && s.getItems() != null) {
                for (com.prolog.clubvip.vo.SaleItemVo it : s.getItems()) {
                    n += it.getQuantity() != null ? it.getQuantity() : 0;
                }
            }
        }
        return n;
    }

    public String saleBadge(String status) {
        if ("COMPLETED".equalsIgnoreCase(status)) {
            return "badge badge-success";
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "badge badge-danger";
        }
        return "badge badge-muted";
    }

    public String saleStatusLabel(String status) {
        if (status == null) {
            return "";
        }
        String key = "pos.saleStatus." + status.toUpperCase();
        String text = sessionMb.text(key);
        return key.equals(text) ? status : text;
    }

    /** Sale timestamp in the business zone, dd/MM/yyyy HH:mm. */
    public String fmtStamp(java.util.Date stamp) {
        if (stamp == null) {
            return "";
        }
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm");
        df.setTimeZone(java.util.TimeZone.getTimeZone(com.prolog.clubvip.constant.Constants.TIMEZONE));
        return df.format(stamp);
    }

    public List<SaleVo> getTodaySales() {
        return todaySales;
    }

    public CreditPortfolioVo getPortfolio() {
        return portfolio;
    }

    /** Builds the birthday WhatsApp message for a customer and opens the wa.me link in a new tab. */
    public void whatsappBirthday(BusinessCustomerVo customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return;
        }
        try {
            MessageComposeVo compose = RestClient.get(
                    "message/compose?templateType=BIRTHDAY&customerId=" + customer.getCustomerId(),
                    MessageComposeVo.class, userId(), sessionMb.getLocale());
            if (compose != null && compose.getWaLink() != null) {
                PrimeFaces.current().executeScript("window.open('" + jsEscape(compose.getWaLink()) + "','_blank')");
            } else if (customer.getWaLink() != null) {
                PrimeFaces.current().executeScript("window.open('" + jsEscape(customer.getWaLink()) + "','_blank')");
            }
        } catch (RestException e) {
            for (String m : e.getMessages()) {
                addMessage(FacesMessage.SEVERITY_ERROR, m);
            }
        }
    }

    /** CSS badge class for an appointment status. */
    public String statusBadge(String status) {
        if (status == null) {
            return "badge badge-muted";
        }
        switch (status) {
            case "DONE":
            case "PAID":
                return "badge badge-success";
            case "IN_PROGRESS":
            case "BOOKED":
                return "badge badge-info";
            case "NO_SHOW":
            case "PENDING":
                return "badge badge-warning";
            case "CANCELLED":
                return "badge badge-danger";
            default:
                return "badge badge-muted";
        }
    }

    public boolean isAttentionNeeded() {
        return nz(summary.getInactiveCount()) > 0 || nz(summary.getPendingReviews()) > 0
                || nz(summary.getBelowMinimumProducts()) > 0 || nz(summary.getPendingPayrolls()) > 0;
    }

    public int getAppointmentsCount() {
        return nz(summary.getAppointmentsCount());
    }

    public int getAttendedCount() {
        return nz(summary.getAttendedCount());
    }

    public int getPendingCount() {
        return nz(summary.getPendingCount());
    }

    public int getCancelledCount() {
        return nz(summary.getCancelledCount());
    }

    public BigDecimal getRevenue() {
        return summary.getRevenue() != null ? summary.getRevenue() : BigDecimal.ZERO;
    }

    public int getTotalCustomers() {
        return nz(summary.getTotalCustomers());
    }

    public int getNewCustomersWeek() {
        return nz(summary.getNewCustomersWeek());
    }

    public BigDecimal getRatingAverage() {
        return summary.getRatingAverage() != null ? summary.getRatingAverage() : BigDecimal.ZERO;
    }

    public int getRatingCount() {
        return nz(summary.getRatingCount());
    }

    /** Gold stars string for the rating average (rounded), e.g. 4 -> "★★★★☆". */
    public String getRatingStars() {
        int full = getRatingAverage().setScale(0, java.math.RoundingMode.HALF_UP).intValue();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? '★' : '☆');
        }
        return sb.toString();
    }

    public List<AppointmentVo> getAppointments() {
        return summary.getAppointments() != null ? summary.getAppointments() : new ArrayList<>();
    }

    public List<BusinessCustomerVo> getBirthdays() {
        return summary.getBirthdays() != null ? summary.getBirthdays() : new ArrayList<>();
    }

    public TodaySummaryVo getSummary() {
        return summary;
    }

    public boolean isLoaded() {
        return loaded;
    }

    private Integer userId() {
        return sessionMb.getUser() != null ? sessionMb.getUser().getUserId() : null;
    }

    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private static String jsEscape(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    private void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, text, null));
        }
    }
}
