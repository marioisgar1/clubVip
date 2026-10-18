package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Traffic of the public links of a business in a period: totals, the trend against the previous
 * period of the same length, series by day and by hour, shares by section / source / device /
 * referrer, the most viewed products and the latest visits.
 */
public class TrafficReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String from;
    private String to;

    // ---- totals
    private long visits;
    private long visitors;
    private long newVisitors;
    private long returningVisitors;
    private long productViews;
    private long waClicks;
    private long bookings;
    /** Visits with at least one product opened. */
    private long visitsWithProducts;
    /** Average seconds per visit (visits with a measured duration). */
    private BigDecimal avgDurationSeconds = BigDecimal.ZERO;
    /** Average products opened per visit. */
    private BigDecimal avgProductsPerVisit = BigDecimal.ZERO;
    /** Bookings + WhatsApp clicks per 100 visits. */
    private BigDecimal conversionPercent = BigDecimal.ZERO;
    /** Visits of the previous period of the same length and the change in %. */
    private long previousVisits;
    private BigDecimal visitsVariationPercent;

    // ---- series
    private List<TrafficPointVo> byDay = new ArrayList<>();
    private List<TrafficPointVo> byHour = new ArrayList<>();
    private List<TrafficPointVo> byWeekday = new ArrayList<>();

    // ---- shares
    private List<TrafficSliceVo> bySection = new ArrayList<>();
    private List<TrafficSliceVo> bySource = new ArrayList<>();
    private List<TrafficSliceVo> byDevice = new ArrayList<>();
    private List<TrafficSliceVo> byReferrer = new ArrayList<>();

    // ---- detail
    private List<TrafficProductVo> topProducts = new ArrayList<>();
    private List<TrafficVisitRowVo> latestVisits = new ArrayList<>();

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }

    public long getVisitors() {
        return visitors;
    }

    public void setVisitors(long visitors) {
        this.visitors = visitors;
    }

    public long getNewVisitors() {
        return newVisitors;
    }

    public void setNewVisitors(long newVisitors) {
        this.newVisitors = newVisitors;
    }

    public long getReturningVisitors() {
        return returningVisitors;
    }

    public void setReturningVisitors(long returningVisitors) {
        this.returningVisitors = returningVisitors;
    }

    public long getProductViews() {
        return productViews;
    }

    public void setProductViews(long productViews) {
        this.productViews = productViews;
    }

    public long getWaClicks() {
        return waClicks;
    }

    public void setWaClicks(long waClicks) {
        this.waClicks = waClicks;
    }

    public long getBookings() {
        return bookings;
    }

    public void setBookings(long bookings) {
        this.bookings = bookings;
    }

    public long getVisitsWithProducts() {
        return visitsWithProducts;
    }

    public void setVisitsWithProducts(long visitsWithProducts) {
        this.visitsWithProducts = visitsWithProducts;
    }

    public BigDecimal getAvgDurationSeconds() {
        return avgDurationSeconds;
    }

    public void setAvgDurationSeconds(BigDecimal avgDurationSeconds) {
        this.avgDurationSeconds = avgDurationSeconds;
    }

    public BigDecimal getAvgProductsPerVisit() {
        return avgProductsPerVisit;
    }

    public void setAvgProductsPerVisit(BigDecimal avgProductsPerVisit) {
        this.avgProductsPerVisit = avgProductsPerVisit;
    }

    public BigDecimal getConversionPercent() {
        return conversionPercent;
    }

    public void setConversionPercent(BigDecimal conversionPercent) {
        this.conversionPercent = conversionPercent;
    }

    public long getPreviousVisits() {
        return previousVisits;
    }

    public void setPreviousVisits(long previousVisits) {
        this.previousVisits = previousVisits;
    }

    public BigDecimal getVisitsVariationPercent() {
        return visitsVariationPercent;
    }

    public void setVisitsVariationPercent(BigDecimal visitsVariationPercent) {
        this.visitsVariationPercent = visitsVariationPercent;
    }

    public List<TrafficPointVo> getByDay() {
        return byDay;
    }

    public void setByDay(List<TrafficPointVo> byDay) {
        this.byDay = byDay;
    }

    public List<TrafficPointVo> getByHour() {
        return byHour;
    }

    public void setByHour(List<TrafficPointVo> byHour) {
        this.byHour = byHour;
    }

    public List<TrafficPointVo> getByWeekday() {
        return byWeekday;
    }

    public void setByWeekday(List<TrafficPointVo> byWeekday) {
        this.byWeekday = byWeekday;
    }

    public List<TrafficSliceVo> getBySection() {
        return bySection;
    }

    public void setBySection(List<TrafficSliceVo> bySection) {
        this.bySection = bySection;
    }

    public List<TrafficSliceVo> getBySource() {
        return bySource;
    }

    public void setBySource(List<TrafficSliceVo> bySource) {
        this.bySource = bySource;
    }

    public List<TrafficSliceVo> getByDevice() {
        return byDevice;
    }

    public void setByDevice(List<TrafficSliceVo> byDevice) {
        this.byDevice = byDevice;
    }

    public List<TrafficSliceVo> getByReferrer() {
        return byReferrer;
    }

    public void setByReferrer(List<TrafficSliceVo> byReferrer) {
        this.byReferrer = byReferrer;
    }

    public List<TrafficProductVo> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TrafficProductVo> topProducts) {
        this.topProducts = topProducts;
    }

    public List<TrafficVisitRowVo> getLatestVisits() {
        return latestVisits;
    }

    public void setLatestVisits(List<TrafficVisitRowVo> latestVisits) {
        this.latestVisits = latestVisits;
    }
}
