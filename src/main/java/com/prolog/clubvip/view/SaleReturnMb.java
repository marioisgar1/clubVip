package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.SaleVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Returns / cancellations (SALE_RETURN): sales of a date range (optionally narrowed by folio),
 * their detail, and the cancellation with a reason, which puts the goods back in stock.
 * sale/getSales?from=&to= · sale/getSale/{id} · sale/cancelSale/{id}?reason=
 */
@Named("saleReturnMb")
@ViewScoped
public class SaleReturnMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type SALE_LIST = new TypeToken<List<SaleVo>>() { }.getType();

    private Date from;
    private Date to;
    private String folio;
    private List<SaleVo> sales = new ArrayList<>();

    private SaleVo detail;
    private SaleVo target;
    private String cancelReason;

    @PostConstruct
    public void init() {
        from = toDate(today().minusDays(7));
        to = toDate(today());
        refresh();
    }

    @Override
    public void refresh() {
        search();
    }

    public void search() {
        if (from != null && to != null && from.after(to)) {
            Date tmp = from;
            from = to;
            to = tmp;
        }
        try {
            List<SaleVo> list = RestClient.get("sale/getSales?from=" + isoDay(from) + "&to=" + isoDay(to),
                    SALE_LIST, userId(), locale());
            sales = filterByFolio(list != null ? list : new ArrayList<>());
        } catch (RestException e) {
            sales = new ArrayList<>();
            errors(e);
        }
    }

    private List<SaleVo> filterByFolio(List<SaleVo> list) {
        String q = folio != null ? folio.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            return list;
        }
        List<SaleVo> out = new ArrayList<>();
        for (SaleVo s : list) {
            if ((s.getFolio() != null && s.getFolio().toLowerCase(Locale.ROOT).contains(q))
                    || (s.getId() != null && String.valueOf(s.getId()).equals(q))) {
                out.add(s);
            }
        }
        return out;
    }

    public void openDetail(SaleVo sale) {
        detail = null;
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
    }

    public void prepareCancel(SaleVo sale) {
        target = sale;
        cancelReason = null;
    }

    public void cancelSale() {
        if (target == null) {
            return;
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            errorKey("pos.return.reasonRequired");
            return;
        }
        try {
            RestClient.post("sale/cancelSale/" + target.getId() + "?reason=" + RestClient.encode(cancelReason.trim()),
                    null, null, userId(), locale());
            info("pos.return.cancelled");
            target = null;
            cancelReason = null;
            search();
            PrimeFaces.current().executeScript("PF('cancelDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public boolean isCancellable(SaleVo sale) {
        return sale != null && "COMPLETED".equalsIgnoreCase(sale.getSaleStatus());
    }

    /** CSS pill for a sale status. */
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

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public List<SaleVo> getSales() {
        return sales;
    }

    public SaleVo getDetail() {
        return detail;
    }

    public SaleVo getTarget() {
        return target;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
