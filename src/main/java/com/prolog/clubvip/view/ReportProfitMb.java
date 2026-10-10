package com.prolog.clubvip.view;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ProfitByVenueVo;
import com.prolog.clubvip.vo.SalesProfitVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Sales and real profit (REPORT_SALES_PROFIT): revenue, cost of goods, card commissions,
 * discounts, venue cost and net profit for a range, plus the breakdown by selling spell
 * (one row per bazaar / venue expense) with a bar chart. report/salesProfit?from=&to=
 */
@Named("reportProfitMb")
@ViewScoped
public class ReportProfitMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate from;
    private LocalDate to;
    private SalesProfitVo report;
    private BarChartModel venueChart;

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
            report = RestClient.get("report/salesProfit?from=" + (from != null ? from.format(FMT) : "")
                    + "&to=" + (to != null ? to.format(FMT) : ""), SalesProfitVo.class, userId(), locale());
        } catch (RestException e) {
            report = null;
            errors(e);
        }
        buildVenueChart();
    }

    /** Revenue and net profit per venue, gold bars for revenue and ink bars for profit. */
    private void buildVenueChart() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet revenue = new BarChartDataSet();
        revenue.setLabel(sessionMb.text("pos.report.revenue"));
        BarChartDataSet profit = new BarChartDataSet();
        profit.setLabel(sessionMb.text("pos.report.netProfit"));
        List<Object> revenueValues = new ArrayList<>();
        List<Object> profitValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (report != null && report.getVenues() != null) {
            for (ProfitByVenueVo v : report.getVenues()) {
                labels.add(venueTitle(v));
                revenueValues.add(v.getRevenue() != null ? v.getRevenue() : BigDecimal.ZERO);
                profitValues.add(v.getNetProfit() != null ? v.getNetProfit() : BigDecimal.ZERO);
            }
        }
        revenue.setData(revenueValues);
        revenue.setBackgroundColor("#F0C756");
        revenue.setBorderColor("#B8902F");
        revenue.setBorderWidth(1);
        profit.setData(profitValues);
        profit.setBackgroundColor("#24262C");
        profit.setBorderColor("#0B0B0C");
        profit.setBorderWidth(1);
        data.addChartDataSet(revenue);
        data.addChartDataSet(profit);
        data.setLabels(labels);
        model.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes y = new CartesianLinearAxes();
        y.setBeginAtZero(true);
        scales.addYAxesData(y);
        options.setScales(scales);
        Legend legend = new Legend();
        legend.setDisplay(true);
        options.setLegend(legend);
        options.setMaintainAspectRatio(false);
        model.setOptions(options);
        venueChart = model;
    }

    public boolean isHasVenues() {
        return report != null && report.getVenues() != null && !report.getVenues().isEmpty();
    }

    /** Headline of a breakdown row: what she called the expense, or the plain location. */
    public String venueTitle(ProfitByVenueVo venue) {
        if (venue == null) {
            return "";
        }
        if (venue.getDescription() != null && !venue.getDescription().trim().isEmpty()) {
            return venue.getDescription().trim();
        }
        return venue.getLocation() != null && !venue.getLocation().trim().isEmpty()
                ? venue.getLocation() : sessionMb.text("pos.report.noLocation");
    }

    /** "Plaza del Valle · 13/03/2026 - 17/03/2026 · 5 days", trimmed of whatever is missing. */
    public String venueSubtitle(ProfitByVenueVo venue) {
        if (venue == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        boolean titled = venue.getDescription() != null && !venue.getDescription().trim().isEmpty();
        if (titled && venue.getLocation() != null && !venue.getLocation().trim().isEmpty()) {
            parts.add(venue.getLocation());
        }
        if (venue.getStartMillis() != null && venue.getEndMillis() != null) {
            parts.add(day(venue.getStartMillis()) + " - " + day(venue.getEndMillis()));
        }
        if (venue.getDays() != null && venue.getDays() > 0) {
            parts.add(venue.getDays() + " " + sessionMb.text(venue.getDays() == 1 ? "pos.report.day" : "pos.report.days"));
        }
        return String.join(" \u00b7 ", parts);
    }

    public BigDecimal getMarginPercent() {
        if (report == null || report.getRevenue() == null || report.getRevenue().signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal net = report.getNetProfit() != null ? report.getNetProfit() : BigDecimal.ZERO;
        return net.multiply(BigDecimal.valueOf(100)).divide(report.getRevenue(), 1, java.math.RoundingMode.HALF_UP);
    }

    private String day(Long millis) {
        return DAY_FMT.format(Instant.ofEpochMilli(millis).atZone(ZoneId.of(Constants.TIMEZONE)));
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

    public SalesProfitVo getReport() {
        return report;
    }

    public BarChartModel getVenueChart() {
        return venueChart;
    }
}
