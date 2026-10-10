package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A venue (location) expense as it appears in the detailed sales report: the cost that falls
 * inside the requested window, plus a chronological anchor so the app can interleave it with the
 * sale cards.
 */
public class ReportVenueCostVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String location;         // sale location name
    private String description;      // expense description (e.g. stand rent)
    private Long dateMillis;         // epoch millis of the overlap start day (for ordering)
    private Integer days;            // days of this expense that fall inside the window
    private BigDecimal dailyCost;
    private BigDecimal amount;        // days * dailyCost (the cost charged to this window)

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(Long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getDailyCost() {
        return dailyCost;
    }

    public void setDailyCost(BigDecimal dailyCost) {
        this.dailyCost = dailyCost;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
