package com.prolog.clubvip.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.StockDiffReportVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Inventory differences (REPORT_STOCK_DIFF): shortages and surpluses found by the closed stock
 * counts of a range. report/stockDifferences?from=&to=
 */
@Named("reportStockDiffMb")
@ViewScoped
public class ReportStockDiffMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDate from;
    private LocalDate to;
    private StockDiffReportVo report;

    @PostConstruct
    public void init() {
        from = today().withDayOfMonth(1);
        to = today();
        generate();
    }

    public void generate() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        try {
            report = RestClient.get("report/stockDifferences?from=" + (from != null ? from.format(FMT) : "")
                    + "&to=" + (to != null ? to.format(FMT) : ""), StockDiffReportVo.class, userId(), locale());
        } catch (RestException e) {
            report = null;
            errors(e);
        }
    }

    public String rowClass(Integer difference) {
        if (difference == null || difference == 0) {
            return "";
        }
        return difference < 0 ? "row-shortage" : "row-surplus";
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public StockDiffReportVo getReport() {
        return report;
    }
}
