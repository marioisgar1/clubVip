package com.prolog.clubvip.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.donut.DonutChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.TrafficPointVo;
import com.prolog.clubvip.vo.TrafficReportVo;
import com.prolog.clubvip.vo.TrafficSliceVo;
import com.prolog.clubvip.vo.TrafficVisitRowVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Traffic of the public links (REPORT_TRAFFIC): traffic/report?from=&to=&section=&source=.
 * Visits and visitors per day, hours and weekdays, shares by section / channel / device /
 * origin, most viewed products and the latest visits.
 */
@Named("reportTrafficMb")
@ViewScoped
public class ReportTrafficMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String LIME = "#CEF187";
    private static final String FOREST = "#5B7720";
    private static final String INK = "#0B0B0C";
    private static final List<String> PALETTE = Arrays.asList("#CEF187", "#5B7720", "#0B0B0C", "#9EC35A", "#E4E2DA", "#B8B6AE");

    private LocalDate from;
    private LocalDate to;
    private String section;
    private String source;

    private TrafficReportVo report = new TrafficReportVo();
    private LineChartModel dayChart;
    private BarChartModel hourChart;
    private BarChartModel weekdayChart;
    private DonutChartModel sectionChart;
    private DonutChartModel sourceChart;
    private DonutChartModel deviceChart;

    @PostConstruct
    public void init() {
        to = today();
        from = to.minusDays(29);
        generate();
    }

    /** Quick ranges of the toolbar: 7, 30 or 90 days up to today. */
    public void range(int days) {
        to = today();
        from = to.minusDays(days - 1L);
        generate();
    }

    public void generate() {
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        try {
            StringBuilder path = new StringBuilder("traffic/report");
            path.append("?from=").append(from != null ? from.format(FMT) : "");
            path.append("&to=").append(to != null ? to.format(FMT) : "");
            if (section != null && !section.isBlank()) {
                path.append("&section=").append(section);
            }
            if (source != null && !source.isBlank()) {
                path.append("&source=").append(source);
            }
            TrafficReportVo r = RestClient.get(path.toString(), TrafficReportVo.class, userId(), locale());
            report = r != null ? r : new TrafficReportVo();
        } catch (RestException e) {
            report = new TrafficReportVo();
            errors(e);
        }
        buildCharts();
    }

    // ------------------------------------------------------------------ charts

    private void buildCharts() {
        dayChart = buildDayChart();
        hourChart = buildBars(report.getByHour(), true);
        weekdayChart = buildBars(report.getByWeekday(), false);
        sectionChart = buildDonut(report.getBySection(), "traffic.section.");
        sourceChart = buildDonut(report.getBySource(), "traffic.source.");
        deviceChart = buildDonut(report.getByDevice(), "traffic.device.");
    }

    private LineChartModel buildDayChart() {
        LineChartModel model = new LineChartModel();
        ChartData data = new ChartData();
        List<String> labels = new ArrayList<>();
        List<Object> visits = new ArrayList<>();
        List<Object> visitors = new ArrayList<>();
        for (TrafficPointVo p : report.getByDay()) {
            labels.add(shortDay(p.getLabel()));
            visits.add(p.getVisits());
            visitors.add(p.getVisitors());
        }
        LineChartDataSet ds1 = new LineChartDataSet();
        ds1.setLabel(sessionMb.text("traffic.kpi.visits"));
        ds1.setData(visits);
        ds1.setBorderColor(FOREST);
        ds1.setBackgroundColor("rgba(206,241,135,.35)");
        ds1.setFill(true);
        ds1.setTension(0.35);
        ds1.setPointRadius(labels.size() > 45 ? 0 : 3);
        LineChartDataSet ds2 = new LineChartDataSet();
        ds2.setLabel(sessionMb.text("traffic.kpi.visitors"));
        ds2.setData(visitors);
        ds2.setBorderColor(INK);
        ds2.setBackgroundColor(INK);
        ds2.setFill(false);
        ds2.setTension(0.35);
        ds2.setBorderDash(Arrays.asList(5, 4));
        ds2.setPointRadius(labels.size() > 45 ? 0 : 3);
        data.addChartDataSet(ds1);
        data.addChartDataSet(ds2);
        data.setLabels(labels);
        model.setData(data);

        LineChartOptions options = new LineChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes y = new CartesianLinearAxes();
        y.setBeginAtZero(true);
        scales.addYAxesData(y);
        options.setScales(scales);
        options.setMaintainAspectRatio(false);
        model.setOptions(options);
        return model;
    }

    /** Hours of the day ("0h".."23h") or weekdays (Mon..Sun) with their visits. */
    private BarChartModel buildBars(List<TrafficPointVo> points, boolean hours) {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet ds = new BarChartDataSet();
        ds.setLabel(sessionMb.text("traffic.kpi.visits"));
        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (TrafficPointVo p : points) {
            values.add(p.getVisits());
            labels.add(hours ? p.getLabel() + "h" : sessionMb.text("traffic.weekday." + p.getLabel()));
        }
        ds.setData(values);
        ds.setBackgroundColor(LIME);
        ds.setBorderColor(FOREST);
        ds.setBorderWidth(1);
        data.addChartDataSet(ds);
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
        return model;
    }

    private DonutChartModel buildDonut(List<TrafficSliceVo> slices, String keyPrefix) {
        DonutChartModel model = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet ds = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        int i = 0;
        for (TrafficSliceVo s : slices) {
            values.add(s.getVisits());
            labels.add(sliceLabel(s.getKey(), keyPrefix));
            colors.add(PALETTE.get(i++ % PALETTE.size()));
        }
        ds.setData(values);
        ds.setBackgroundColor(colors);
        data.addChartDataSet(ds);
        data.setLabels(labels);
        model.setData(data);
        DonutChartOptions options = new DonutChartOptions();
        options.setMaintainAspectRatio(false);
        Legend legend = new Legend();
        legend.setPosition("bottom");
        options.setLegend(legend);
        model.setOptions(options);
        return model;
    }

    /** Translated label of a section / source / device key; the key itself for referrer hosts. */
    public String sliceLabel(String key, String keyPrefix) {
        if (key == null) {
            return "";
        }
        String text = sessionMb.text(keyPrefix + key);
        return text == null || text.startsWith("???") ? key : text;
    }

    /** "yyyy-MM-dd" -> "dd/MM". */
    private static String shortDay(String day) {
        if (day == null || day.length() < 10) {
            return day != null ? day : "";
        }
        return day.substring(8, 10) + "/" + day.substring(5, 7);
    }

    // ------------------------------------------------------------------ presentation helpers

    /** Origin label of a visit row / referrer slice: translated for the apps and DIRECT, the host otherwise. */
    public String originLabel(String key) {
        if (key == null) {
            return "";
        }
        if ("DIRECT".equals(key) || "OTHER".equals(key) || "APP".equals(key) || "REWARDS".equals(key)) {
            return sessionMb.text("traffic.origin." + key);
        }
        return key;
    }

    /** Seconds -> "1 min 20 s" / "45 s" / "—". */
    public String fmtDuration(Number seconds) {
        if (seconds == null || seconds.longValue() <= 0) {
            return "—";
        }
        long s = seconds.longValue();
        if (s < 60) {
            return s + " s";
        }
        long m = s / 60;
        long rest = s % 60;
        if (m >= 60) {
            return (m / 60) + " h " + (m % 60) + " min";
        }
        return rest > 0 ? m + " min " + rest + " s" : m + " min";
    }

    public String getVariationBadge() {
        BigDecimal v = report.getVisitsVariationPercent();
        if (v == null) {
            return "badge badge-muted";
        }
        return v.signum() > 0 ? "badge badge-success" : (v.signum() < 0 ? "badge badge-danger" : "badge badge-muted");
    }

    public String getVariationText() {
        BigDecimal v = report.getVisitsVariationPercent();
        if (v == null) {
            return sessionMb.text("traffic.kpi.noPrevious");
        }
        return (v.signum() > 0 ? "+" : "") + v.toPlainString() + "%";
    }

    public boolean isHasData() {
        return report.getVisits() > 0;
    }

    public int getDays() {
        return report.getByDay() != null ? report.getByDay().size() : 0;
    }

    /** Visits per day on average. */
    public BigDecimal getVisitsPerDay() {
        int d = getDays();
        if (d == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(report.getVisits()).divide(BigDecimal.valueOf(d), 1, java.math.RoundingMode.HALF_UP);
    }

    public String rowBadge(TrafficVisitRowVo r) {
        return r.getBookings() > 0 ? "badge badge-success" : (r.getWaClicks() > 0 ? "badge badge-warning" : "badge badge-muted");
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public TrafficReportVo getReport() {
        return report;
    }

    public LineChartModel getDayChart() {
        return dayChart;
    }

    public BarChartModel getHourChart() {
        return hourChart;
    }

    public BarChartModel getWeekdayChart() {
        return weekdayChart;
    }

    public DonutChartModel getSectionChart() {
        return sectionChart;
    }

    public DonutChartModel getSourceChart() {
        return sourceChart;
    }

    public DonutChartModel getDeviceChart() {
        return deviceChart;
    }
}
