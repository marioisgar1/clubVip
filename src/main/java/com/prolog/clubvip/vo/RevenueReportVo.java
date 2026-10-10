package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Revenue report for a date range with a daily breakdown and comparison with the previous period.
 */
public class RevenueReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String from;

    private String to;

    private BigDecimal total;

    private Integer appointments;

    private List<DayRevenueVo> days = new ArrayList<>();

    private BigDecimal previousPeriodTotal;

    private BigDecimal variationPercent;

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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getAppointments() {
        return appointments;
    }

    public void setAppointments(Integer appointments) {
        this.appointments = appointments;
    }

    public List<DayRevenueVo> getDays() {
        return days;
    }

    public void setDays(List<DayRevenueVo> days) {
        this.days = days;
    }

    public BigDecimal getPreviousPeriodTotal() {
        return previousPeriodTotal;
    }

    public void setPreviousPeriodTotal(BigDecimal previousPeriodTotal) {
        this.previousPeriodTotal = previousPeriodTotal;
    }

    public BigDecimal getVariationPercent() {
        return variationPercent;
    }

    public void setVariationPercent(BigDecimal variationPercent) {
        this.variationPercent = variationPercent;
    }
}
