package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;

import com.prolog.clubvip.rest.GpsClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.GpsMinuteVo;
import com.prolog.clubvip.vo.GpsStatsVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * GPS › Estadísticas (Club VIP operator): devices online / silent / offline, frames per minute
 * (average, peak, chart) and receiver health (errors, drops, write queue) from clubVipGps /stats.
 */
@Named("gpsStatsMb")
@ViewScoped
public class GpsStatsMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String GOLD = "#B8902F";
    private static final String INK = "#17181C";
    private static final String DANGER = "#A11A2A";

    @Inject
    private GpsZoneMb gpsZoneMb;

    @Inject
    private SessionMb sessionMb;

    private int minutes = 60;
    private GpsStatsVo stats;
    private boolean gpsDown;
    private String gpsError;
    private LineChartModel chart;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            stats = GpsClient.get("stats?minutes=" + minutes, GpsStatsVo.class);
            gpsDown = stats == null;
            gpsError = null;
        } catch (RestException e) {
            stats = null;
            gpsDown = true;
            gpsError = String.join(" ", e.getMessages());
        }
        chart = stats != null ? buildChart(stats.getPerMinute()) : null;
    }

    public void window(int value) {
        minutes = value;
        refresh();
    }

    private LineChartModel buildChart(List<GpsMinuteVo> rows) {
        List<String> labels = new ArrayList<>();
        List<Object> frames = new ArrayList<>();
        List<Object> keepalives = new ArrayList<>();
        List<Object> errors = new ArrayList<>();
        if (rows != null) {
            for (GpsMinuteVo r : rows) {
                labels.add(GpsFmt.hourMin(r.getMinute(), gpsZoneMb.getZoneId()));
                frames.add(n(r.getFrames()));
                keepalives.add(n(r.getKeepalives()));
                errors.add(n(r.getErrors()) + n(r.getDropped()));
            }
        }
        boolean dense = labels.size() > 60;
        ChartData data = new ChartData();
        data.addChartDataSet(dataSet(sessionMb.text("gps.stats.chart.frames"), frames, GOLD, "rgba(240,199,86,.25)", true, dense));
        data.addChartDataSet(dataSet(sessionMb.text("gps.stats.chart.keepalives"), keepalives, INK, INK, false, dense));
        data.addChartDataSet(dataSet(sessionMb.text("gps.stats.chart.errors"), errors, DANGER, DANGER, false, dense));
        data.setLabels(labels);
        LineChartModel model = new LineChartModel();
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

    private static LineChartDataSet dataSet(String label, List<Object> values, String border, String background, boolean fill, boolean dense) {
        LineChartDataSet ds = new LineChartDataSet();
        ds.setLabel(label);
        ds.setData(values);
        ds.setBorderColor(border);
        ds.setBackgroundColor(background);
        ds.setFill(fill);
        ds.setTension(0.3);
        ds.setPointRadius(dense ? 0 : 2);
        return ds;
    }

    private static long n(Long v) {
        return v != null ? v : 0L;
    }

    // ------------------------------------------------------------------ view helpers

    /** STT, ALT, ALV... with their totals, alphabetical. */
    public List<Map.Entry<String, Long>> getByHeader() {
        Map<String, Long> m = stats != null && stats.getTotalByHeader() != null ? new TreeMap<>(stats.getTotalByHeader()) : new TreeMap<>();
        return new ArrayList<>(m.entrySet());
    }

    public String headerText(String header) {
        String key = "gps.header." + header;
        String t = sessionMb.text(key);
        return t == null || t.equals(key) || t.startsWith("???") ? header : header + " · " + t;
    }

    public String dateTime(String iso) {
        return GpsFmt.dateTime(iso, gpsZoneMb.getZoneId());
    }

    public String duration(Long seconds) {
        return GpsFmt.duration(seconds);
    }

    public String getUptime() {
        return stats != null ? GpsFmt.duration(stats.getUptimeSeconds()) : "—";
    }

    public String getAvg() {
        return stats != null && stats.getFramesPerMinuteAvg() != null ? String.format(java.util.Locale.ROOT, "%.1f", stats.getFramesPerMinuteAvg()) : "0";
    }

    public boolean isHealthy() {
        return stats != null && n(stats.getDroppedInWindow()) == 0 && stats.getWriteQueue() != null && stats.getWriteQueue() < 1000;
    }

    // ------------------------------------------------------------------ getters

    public int getMinutes() { return minutes; }
    public GpsStatsVo getStats() { return stats; }
    public boolean isGpsDown() { return gpsDown; }
    public String getGpsError() { return gpsError; }
    public LineChartModel getChart() { return chart; }
}
