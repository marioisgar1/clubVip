package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
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

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CustomerStatsVo;
import com.prolog.clubvip.vo.DailyReportVo;
import com.prolog.clubvip.vo.DayRevenueVo;
import com.prolog.clubvip.vo.NameValueVo;
import com.prolog.clubvip.vo.OccupancyVo;
import com.prolog.clubvip.vo.ProductMarginVo;
import com.prolog.clubvip.vo.RevenueReportVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Business reports: day, revenue (with chart), top services, occupancy, product margin and customers.
 */
@Named("reportMb")
@ViewScoped
public class ReportMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Type NAME_VALUE_LIST = new TypeToken<List<NameValueVo>>() { }.getType();
    private static final Type OCCUPANCY_LIST = new TypeToken<List<OccupancyVo>>() { }.getType();
    private static final Type MARGIN_LIST = new TypeToken<List<ProductMarginVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private LocalDate date;
    private LocalDate from;
    private LocalDate to;

    private DailyReportVo daily = new DailyReportVo();
    private RevenueReportVo revenue = new RevenueReportVo();
    private BarChartModel revenueChart;
    private List<NameValueVo> topServices = new ArrayList<>();
    private List<OccupancyVo> occupancy = new ArrayList<>();
    private List<ProductMarginVo> productMargin = new ArrayList<>();
    private CustomerStatsVo customerStats = new CustomerStatsVo();

    @PostConstruct
    public void init() {
        LocalDate today = LocalDate.now(ZoneId.of(Constants.TIMEZONE));
        date = today;
        from = today.withDayOfMonth(1);
        to = today;
        refresh();
    }

    public void refresh() {
        loadDaily();
        loadRevenue();
        loadTopServices();
        loadOccupancy();
        loadProductMargin();
        loadCustomerStats();
    }

    // ------------------------------------------------------------------ loaders

    public void loadDaily() {
        try {
            DailyReportVo d = RestClient.get("report/daily?date=" + iso(date), DailyReportVo.class, userId(), locale());
            daily = d != null ? d : new DailyReportVo();
        } catch (RestException e) {
            daily = new DailyReportVo();
            errors(e);
        }
    }

    public void loadRevenue() {
        try {
            RevenueReportVo r = RestClient.get("report/revenue?from=" + iso(from) + "&to=" + iso(to),
                    RevenueReportVo.class, userId(), locale());
            revenue = r != null ? r : new RevenueReportVo();
        } catch (RestException e) {
            revenue = new RevenueReportVo();
            errors(e);
        }
        buildRevenueChart();
    }

    public void loadTopServices() {
        try {
            List<NameValueVo> list = RestClient.get("report/topServices?from=" + iso(from) + "&to=" + iso(to),
                    NAME_VALUE_LIST, userId(), locale());
            topServices = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            topServices = new ArrayList<>();
            errors(e);
        }
    }

    public void loadOccupancy() {
        try {
            List<OccupancyVo> list = RestClient.get("report/occupancy?from=" + iso(from), OCCUPANCY_LIST, userId(), locale());
            occupancy = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            occupancy = new ArrayList<>();
            errors(e);
        }
    }

    public void loadProductMargin() {
        try {
            List<ProductMarginVo> list = RestClient.get("report/productMargin?from=" + iso(from) + "&to=" + iso(to),
                    MARGIN_LIST, userId(), locale());
            productMargin = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            productMargin = new ArrayList<>();
            errors(e);
        }
    }

    public void loadCustomerStats() {
        try {
            CustomerStatsVo s = RestClient.get("report/customerStats", CustomerStatsVo.class, userId(), locale());
            customerStats = s != null ? s : new CustomerStatsVo();
        } catch (RestException e) {
            customerStats = new CustomerStatsVo();
            errors(e);
        }
    }

    /** Range changed: reload every range-based tab. */
    public void applyRange() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        loadRevenue();
        loadTopServices();
        loadOccupancy();
        loadProductMargin();
    }

    // ------------------------------------------------------------------ chart

    private void buildRevenueChart() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel(sessionMb.text("rel.report.revenue"));
        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (revenue.getDays() != null) {
            for (DayRevenueVo d : revenue.getDays()) {
                values.add(d.getRevenue() != null ? d.getRevenue() : BigDecimal.ZERO);
                labels.add(shortDay(d.getDate()));
            }
        }
        dataSet.setData(values);
        dataSet.setBackgroundColor("#F0C756");
        dataSet.setBorderColor("#B8902F");
        dataSet.setBorderWidth(1);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes y = new CartesianLinearAxes();
        y.setBeginAtZero(true);
        scales.addYAxesData(y);
        options.setScales(scales);
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        options.setMaintainAspectRatio(false);
        model.setOptions(options);
        revenueChart = model;
    }

    /** "yyyy-MM-dd" -> "dd/MM" for chart labels. */
    private String shortDay(String day) {
        if (day == null || day.length() < 10) {
            return day != null ? day : "";
        }
        return day.substring(8, 10) + "/" + day.substring(5, 7);
    }

    public boolean isHasRevenueDays() {
        return revenue.getDays() != null && !revenue.getDays().isEmpty();
    }

    // ------------------------------------------------------------------ presentation helpers

    public BigDecimal getVariation() {
        return revenue.getVariationPercent() != null ? revenue.getVariationPercent() : BigDecimal.ZERO;
    }

    public String getVariationBadge() {
        int sign = getVariation().signum();
        if (sign > 0) {
            return "badge badge-success";
        }
        if (sign < 0) {
            return "badge badge-danger";
        }
        return "badge badge-muted";
    }

    public int occupancyValue(OccupancyVo o) {
        if (o == null || o.getOccupancyPercent() == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, o.getOccupancyPercent().intValue()));
    }

    public String paymentMethodLabel(String code) {
        if (code == null) {
            return "";
        }
        String key = "rel.paymentMethod." + code;
        String text = sessionMb.text(key);
        return key.equals(text) ? code : text;
    }

    public BigDecimal getMarginTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (ProductMarginVo p : productMargin) {
            if (p.getMargin() != null) {
                sum = sum.add(p.getMargin());
            }
        }
        return sum;
    }

    public BigDecimal getMarginRevenueTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (ProductMarginVo p : productMargin) {
            if (p.getRevenue() != null) {
                sum = sum.add(p.getRevenue());
            }
        }
        return sum;
    }

    // ------------------------------------------------------------------ helpers

    private String iso(LocalDate d) {
        return d != null ? d.format(ISO_DAY) : "";
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

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

    public DailyReportVo getDaily() {
        return daily;
    }

    public RevenueReportVo getRevenue() {
        return revenue;
    }

    public BarChartModel getRevenueChart() {
        return revenueChart;
    }

    public List<NameValueVo> getTopServices() {
        return topServices;
    }

    public List<OccupancyVo> getOccupancy() {
        return occupancy;
    }

    public List<ProductMarginVo> getProductMargin() {
        return productMargin;
    }

    public CustomerStatsVo getCustomerStats() {
        return customerStats;
    }
}
