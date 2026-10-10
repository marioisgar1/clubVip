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

    private Date from;
    private Date to;
    private List<SaleVo> sales = new ArrayList<>();
    private List<SaleVo> filtered;
    private SaleVo detail;
    private CreditPlanVo detailCredit;

    @PostConstruct
    public void init() {
        from = toDate(sessionMb.isSeller() ? today() : today().withDayOfMonth(1));
        to = toDate(today());
        refresh();
    }

    @Override
    public void refresh() {
        if (from != null && to != null && from.after(to)) {
            Date tmp = from;
            from = to;
            to = tmp;
        }
        try {
            List<SaleVo> list = RestClient.get("sale/getSales?from=" + isoDay(from) + "&to=" + isoDay(to),
                    SALE_LIST, userId(), locale());
            sales = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            sales = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    public void openDetail(SaleVo sale) {
        detail = null;
        detailCredit = null;
        if (sale == null || sale.getId() == null) {
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
