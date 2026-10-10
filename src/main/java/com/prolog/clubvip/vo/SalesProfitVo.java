package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated profit-and-loss figures for a date range, plus the same figures broken down by
 * selling spell in {@link #getVenues()} — which bazaar actually paid off, and which one only
 * looked busy.
 */
public class SalesProfitVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal revenue = BigDecimal.ZERO;

    private BigDecimal cogs = BigDecimal.ZERO;

    private BigDecimal cardCommissions = BigDecimal.ZERO;

    private BigDecimal discountsGiven = BigDecimal.ZERO;

    private BigDecimal venueCost = BigDecimal.ZERO;

    private BigDecimal netProfit = BigDecimal.ZERO;

    private int salesCount;

    /** Breakdown by location expense, with the sales made outside any of them at the end. */
    private List<ProfitByVenueVo> venues = new ArrayList<>();

    public List<ProfitByVenueVo> getVenues() {
        return venues;
    }

    public void setVenues(List<ProfitByVenueVo> venues) {
        this.venues = venues;
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

    public BigDecimal getCardCommissions() {
        return cardCommissions;
    }

    public void setCardCommissions(BigDecimal cardCommissions) {
        this.cardCommissions = cardCommissions;
    }

    public BigDecimal getDiscountsGiven() {
        return discountsGiven;
    }

    public void setDiscountsGiven(BigDecimal discountsGiven) {
        this.discountsGiven = discountsGiven;
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
}
