package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.StockItemVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Products to restock (REPORT_RESTOCK): every product whose stock reached its minimum
 * (stock &lt;= minimum; with minimum 0 that is the ones at zero or oversold), split into the
 * current line and the outlet, since an outlet (discontinued) product usually can no longer be
 * restocked. Over inventory/getStock. Not the backend's belowMinimum flag: that one ignores
 * products without a minimum, which is right for alerts but not for this report.
 */
@Named("reportRestockMb")
@ViewScoped
public class ReportRestockMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type STOCK_LIST = new TypeToken<List<StockItemVo>>() { }.getType();

    /** Most urgent first: out of stock, then the biggest shortage, then by name. */
    private static final Comparator<StockItemVo> URGENCY = Comparator
            .comparing((StockItemVo s) -> nz(s.getQuantityOnHand()) > 0)
            .thenComparing(StockItemVo::getShortage, Comparator.reverseOrder())
            .thenComparing(s -> s.getName() != null ? s.getName() : "");

    private List<StockItemVo> line = new ArrayList<>();
    private List<StockItemVo> outlet = new ArrayList<>();

    @PostConstruct
    public void init() {
        load();
    }

    public void load() {
        line = new ArrayList<>();
        outlet = new ArrayList<>();
        try {
            List<StockItemVo> stock = RestClient.get("inventory/getStock", STOCK_LIST, userId(), locale());
            if (stock != null) {
                for (StockItemVo s : stock) {
                    if (nz(s.getQuantityOnHand()) > nz(s.getMinStock())) {
                        continue;
                    }
                    (Boolean.TRUE.equals(s.getIsOutlet()) ? outlet : line).add(s);
                }
            }
        } catch (RestException e) {
            errors(e);
        }
        line.sort(URGENCY);
        outlet.sort(URGENCY);
    }

    // ---- per-tab figures (EL: reportRestockMb.outOfStock(reportRestockMb.line), ...)

    /** Products at zero (or oversold). */
    public int outOfStock(List<StockItemVo> rows) {
        int n = 0;
        for (StockItemVo s : rows) {
            if (nz(s.getQuantityOnHand()) <= 0) n++;
        }
        return n;
    }

    /** Units needed to get every product back to its minimum. */
    public int shortageUnits(List<StockItemVo> rows) {
        int n = 0;
        for (StockItemVo s : rows) {
            n += s.getShortage();
        }
        return n;
    }

    /** Estimated cost of those units at average cost. */
    public BigDecimal shortageCost(List<StockItemVo> rows) {
        BigDecimal total = BigDecimal.ZERO;
        for (StockItemVo s : rows) {
            total = total.add(s.getShortageCost());
        }
        return total;
    }

    public String rowClass(StockItemVo s) {
        return nz(s.getQuantityOnHand()) <= 0 ? "row-shortage" : "";
    }

    public List<StockItemVo> getLine() {
        return line;
    }

    public List<StockItemVo> getOutlet() {
        return outlet;
    }
}
