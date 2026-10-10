package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Detailed sales report: every sale (grouped) plus grand totals for the range. */
public class SalesDetailReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SaleDetailVo> sales = new ArrayList<>();
    private List<ReportVenueCostVo> venueExpenses = new ArrayList<>();
    /** The same money cut by selling spell: one row per bazaar, leftovers grouped by location. */
    private List<ProfitByVenueVo> venues = new ArrayList<>();
    private int salesCount;
    private BigDecimal totalRevenue;      // sum of subtotals
    private BigDecimal totalDiscounts;
    private BigDecimal totalCommissions;
    private BigDecimal totalCogs;
    private BigDecimal totalVenueCost;    // venue expenses that fall inside the window
    private BigDecimal totalProfit;       // subtotal - cogs - commissions - venue cost

    public List<SaleDetailVo> getSales() {
        return sales;
    }

    public void setSales(List<SaleDetailVo> sales) {
        this.sales = sales;
    }

    public List<ReportVenueCostVo> getVenueExpenses() {
        return venueExpenses;
    }

    public void setVenueExpenses(List<ReportVenueCostVo> venueExpenses) {
        this.venueExpenses = venueExpenses;
    }

    public List<ProfitByVenueVo> getVenues() {
        return venues;
    }

    public void setVenues(List<ProfitByVenueVo> venues) {
        this.venues = venues;
    }

    public BigDecimal getTotalVenueCost() {
        return totalVenueCost;
    }

    public void setTotalVenueCost(BigDecimal totalVenueCost) {
        this.totalVenueCost = totalVenueCost;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalDiscounts() {
        return totalDiscounts;
    }

    public void setTotalDiscounts(BigDecimal totalDiscounts) {
        this.totalDiscounts = totalDiscounts;
    }

    public BigDecimal getTotalCommissions() {
        return totalCommissions;
    }

    public void setTotalCommissions(BigDecimal totalCommissions) {
        this.totalCommissions = totalCommissions;
    }

    public BigDecimal getTotalCogs() {
        return totalCogs;
    }

    public void setTotalCogs(BigDecimal totalCogs) {
        this.totalCogs = totalCogs;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }
}
