package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Profit of one selling spell: a registered location expense ("BAZAR PLAZA DEL VALLE, Fri to
 * Sun") together with every sale made at that location on those days.
 *
 * <p>Sales that fall outside any registered expense are grouped by their location with
 * {@link #isHasExpense()} false and no venue cost — the business sells from home too, and
 * that is not a loss, it is just a spot that costs nothing.
 *
 * <p>The rows always add up to the totals on top of the report: the venue cost of each row is
 * the same day-by-day proration the general figure uses, and no sale lands in two rows.
 */
public class ProfitByVenueVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Sale location name; the fallback bucket for sales saved without one. */
    private String location;
    /** What the expense was called, e.g. "BAZAR PLAZA DEL VALLE"; null without an expense. */
    private String description;

    /** Window actually charged, already clipped to the report range. Null without an expense. */
    private Long startMillis;
    private Long endMillis;
    /** Days of the expense that fall inside the report range. */
    private Integer days;

    /** False on the buckets built from sales with no registered expense. */
    private boolean hasExpense;

    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal cogs = BigDecimal.ZERO;
    private BigDecimal commissions = BigDecimal.ZERO;
    private BigDecimal discounts = BigDecimal.ZERO;
    private BigDecimal venueCost = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private int salesCount;

    /** Net profit over revenue, 0..100; null when the row sold nothing. */
    private BigDecimal marginPercent;

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

    public Long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(Long startMillis) {
        this.startMillis = startMillis;
    }

    public Long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(Long endMillis) {
        this.endMillis = endMillis;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public boolean isHasExpense() {
        return hasExpense;
    }

    public void setHasExpense(boolean hasExpense) {
        this.hasExpense = hasExpense;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }

    public BigDecimal getCommissions() {
        return commissions;
    }

    public void setCommissions(BigDecimal commissions) {
        this.commissions = commissions;
    }

    public BigDecimal getDiscounts() {
        return discounts;
    }

    public void setDiscounts(BigDecimal discounts) {
        this.discounts = discounts;
    }

    public BigDecimal getVenueCost() {
        return venueCost;
    }

    public void setVenueCost(BigDecimal venueCost) {
        this.venueCost = venueCost;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public BigDecimal getMarginPercent() {
        return marginPercent;
    }

    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent;
    }
}
