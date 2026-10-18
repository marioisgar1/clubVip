package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CreditPlanVo;
import com.prolog.clubvip.vo.SaleVo;
import com.prolog.clubvip.vo.SellerSummaryVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Sales history (SALES_HISTORY): tickets of a date range with totals and a detail dialog. The
 * backend narrows the list for sellers (only their own sales); admins see every sale.
 * sale/getSales?from=&to= · sale/getSale/{id} · credit/getCreditPlanBySale/{saleId}
 */
@Named("salesHistoryMb")
@ViewScoped
public class SalesHistoryMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type SALE_LIST = new TypeToken<List<SaleVo>>() { }.getType();
    private static final Type SELLER_LIST = new TypeToken<List<SellerSummaryVo>>() { }.getType();

    private Date from;
    private Date to;
    private List<SaleVo> sales = new ArrayList<>();
    private List<SaleVo> filtered;
    /** Administrator only: sales, costs and profit per seller in the range (no detail). */
    private List<SellerSummaryVo> sellers = new ArrayList<>();
    private SaleVo detail;
    private CreditPlanVo detailCredit;
    /** Quick range in use (same buttons as reportProfit); empty once a range is picked by hand. */
    private String preset = "";

    @PostConstruct
    public void init() {
        applyPreset(sessionMb.isSeller() ? "today" : "month");
    }

    /** Quick ranges, same as ReportProfitMb: today, week (Mon..today), month, lastMonth, year. */
    public void applyPreset(String code) {
        java.time.LocalDate today = today();
        java.time.LocalDate start;
        java.time.LocalDate end = today;
        preset = code;
        switch (code) {
            case "today":
                start = today;
                break;
            case "week":
                start = today.with(java.time.DayOfWeek.MONDAY);
                break;
            case "lastMonth":
                start = today.minusMonths(1).withDayOfMonth(1);
                end = start.withDayOfMonth(start.lengthOfMonth());
                break;
            case "year":
                start = today.withDayOfYear(1);
                break;
            default:
                start = today.withDayOfMonth(1);
                break;
        }
        from = toDate(start);
        to = toDate(end);
        refresh();
    }

    /** The "Generar" button: a hand-picked range. */
    public void generate() {
        preset = "";
        refresh();
    }

    public String getPreset() {
        return preset;
    }

    @Override
    public void refresh() {
        if (from != null && to != null && from.after(to)) {
            Date tmp = from;
            from = to;
            to = tmp;
        }
        try {
            // The administrator also gets what partner stores sold of her products (consignment).
            List<SaleVo> list = RestClient.get("sale/getSales?from=" + isoDay(from) + "&to=" + isoDay(to)
                    + (sessionMb.isAdmin() ? "&includeConsignment=true" : ""), SALE_LIST, userId(), locale());
            sales = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            sales = new ArrayList<>();
            errors(e);
        }
        filtered = null;
        sellers = new ArrayList<>();
        if (sessionMb.isAdmin()) {
            try {
                List<SellerSummaryVo> list = RestClient.get("report/sellerSummary?from=" + isoDay(from) + "&to=" + isoDay(to),
                        SELLER_LIST, userId(), locale());
                sellers = list != null ? list : new ArrayList<>();
            } catch (RestException e) {
                sellers = new ArrayList<>(); // the table still shows; the summary is a bonus
            }
        }
    }

    public List<SellerSummaryVo> getSellers() {
        return sellers;
    }

    /** What the staff earned on the sales of the range (sum of the per-seller summary). */
    public BigDecimal getTotalStaffCommission() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SellerSummaryVo se : sellers) {
            if (se.getStaffCommission() != null) sum = sum.add(se.getStaffCommission());
        }
        return sum;
    }

    /** More than one person sold in the range: worth showing the per-seller summary. */
    public boolean isShowSellers() {
        return sessionMb.isAdmin() && sellers.size() > 1;
    }

    public void openDetail(SaleVo sale) {
        detail = null;
        detailCredit = null;
        if (sale == null || sale.getId() == null) {
            return;
        }
        if (isVisit(sale)) {
            detail = sale; // a visit charged in the agenda already carries its services and products
            return;
        }
        if (sale.isConsignment()) {
            // A partner store's ticket: only her lines, no customer and no credit plan of hers.
            try {
                SaleVo full = RestClient.get("sale/getSale/" + sale.getId(), SaleVo.class, userId(), locale());
                detail = full != null ? full : sale;
            } catch (RestException e) {
                detail = sale;
                errors(e);
            }
            return;
        }
        try {
            SaleVo full = RestClient.get("sale/getSale/" + sale.getId(), SaleVo.class, userId(), locale());
            detail = full != null ? full : sale;
        } catch (RestException e) {
            detail = sale;
            errors(e);
        }
        if (detail.getCredit() != null) {
            detailCredit = detail.getCredit();
        } else {
            try {
                detailCredit = RestClient.get("credit/getCreditPlanBySale/" + sale.getId(), CreditPlanVo.class, userId(), locale());
            } catch (RestException ignored) {
                detailCredit = null; // cash ticket: no plan
            }
        }
    }

    // ------------------------------------------------------------------ totals

    public int getCompletedCount() {
        int n = 0;
        for (SaleVo s : sales) {
            if (isLive(s)) {
                n++;
            }
        }
        return n;
    }

    public int getCancelledCount() {
        int n = 0;
        for (SaleVo s : sales) {
            if ("CANCELLED".equalsIgnoreCase(s.getSaleStatus())) {
                n++;
            }
        }
        return n;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleVo s : sales) {
            if (isLive(s) && s.getTotal() != null) {
                sum = sum.add(s.getTotal());
            }
        }
        return sum;
    }

    public BigDecimal getTotalCommission() {
        BigDecimal sum = BigDecimal.ZERO;
        for (SaleVo s : sales) {
            if (isLive(s) && s.getCommissionAmount() != null) {
                sum = sum.add(s.getCommissionAmount());
            }
        }
        return sum;
    }

    /** Row of a visit charged in the agenda (services + products), not a point-of-sale ticket. */
    public boolean isVisit(SaleVo s) {
        return s != null && "APPOINTMENT".equals(s.getSaleType());
    }

    private static boolean isLive(SaleVo s) {
        return s != null && !"CANCELLED".equalsIgnoreCase(s.getSaleStatus());
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

    public String statusLabel(String status) {
        if (status == null) {
            return "";
        }
        String key = "pos.saleStatus." + status.toUpperCase(Locale.ROOT);
        String text = sessionMb.text(key);
        return key.equals(text) ? status : text;
    }

    public String typeLabel(String type) {
        if (type == null) {
            return "";
        }
        String key = "pos.saleType." + type.toUpperCase(Locale.ROOT);
        String text = sessionMb.text(key);
        return key.equals(text) ? type : text;
    }

    // ------------------------------------------------------------------ getters / setters

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public List<SaleVo> getSales() {
        return sales;
    }

    public List<SaleVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<SaleVo> filtered) {
        this.filtered = filtered;
    }

    public SaleVo getDetail() {
        return detail;
    }

    public CreditPlanVo getDetailCredit() {
        return detailCredit;
    }
}
