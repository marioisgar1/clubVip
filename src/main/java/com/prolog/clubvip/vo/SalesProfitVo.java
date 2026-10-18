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
    /** Commissions earned by the staff on their sales. */
    private BigDecimal staffCommissions = BigDecimal.ZERO;

    private BigDecimal discountsGiven = BigDecimal.ZERO;

    private BigDecimal venueCost = BigDecimal.ZERO;

    private BigDecimal netProfit = BigDecimal.ZERO;

    private int salesCount;

    /** Revenue split: point of sale tickets vs. visits charged in the agenda (services / products). */
    private BigDecimal posRevenue = BigDecimal.ZERO;
    private BigDecimal visitServices = BigDecimal.ZERO;
    private BigDecimal visitProducts = BigDecimal.ZERO;
    private int visitsCount;

    /** Running expenses of the business (Mis gastos) in the range, and the profit once they are taken off. */
    private BigDecimal businessExpenses = BigDecimal.ZERO;

    private BigDecimal profitAfterExpenses = BigDecimal.ZERO;

    private java.util.Map<String, BigDecimal> businessExpensesByCategory = new java.util.LinkedHashMap<>();

    /** Breakdown by location expense, with the sales made outside any of them at the end. */
    private List<ProfitByVenueVo> venues = new ArrayList<>();

    /** Loyalty rewards taken off services charged in the agenda (POS discounts are in discountsGiven). */
    private BigDecimal visitDiscounts = BigDecimal.ZERO;
    /** Revenue before discounts and rewards: revenue + discountsGiven + visitDiscounts. */
    private BigDecimal grossSales = BigDecimal.ZERO;
    /** staffCommissions split: earned on agenda services / on point-of-sale tickets. */
    private BigDecimal serviceStaffCommissions = BigDecimal.ZERO;
    private BigDecimal saleStaffCommissions = BigDecimal.ZERO;
    /** Fixed salaries accrued in the range (SALARY / MIXED employees), see ProfitPayrollVo. */
    private BigDecimal salaries = BigDecimal.ZERO;
    /** revenue - cogs. */
    private BigDecimal grossProfit = BigDecimal.ZERO;
    /** grossProfit - card commissions - staff commissions. */
    private BigDecimal contributionMargin = BigDecimal.ZERO;
    /** salaries + venueCost + businessExpenses. */
    private BigDecimal operatingExpenses = BigDecimal.ZERO;
    /** The bottom line: contributionMargin - operatingExpenses (before taxes). */
    private BigDecimal realProfit = BigDecimal.ZERO;

    /** Vendido en tiendas aliadas (ya incluido en revenue) */
    private BigDecimal consignmentRevenue = BigDecimal.ZERO;
    /** Comisión de las tiendas aliadas (se resta para el margen real) */
    private BigDecimal consignmentCommissions = BigDecimal.ZERO;
    /** Ventas en consignación del periodo */
    private int consignmentCount;

    /** Every ticket, visit, expense and salary of the range, oldest first. */
    private List<ProfitMovementVo> movements = new ArrayList<>();
    /** Cost of each employee (commissions + salary). */
    private List<ProfitPayrollVo> payroll = new ArrayList<>();
    /** Result per calendar day with activity. */
    private List<ProfitDayVo> days = new ArrayList<>();

    public BigDecimal getVisitDiscounts() { return visitDiscounts; }
    public void setVisitDiscounts(BigDecimal visitDiscounts) { this.visitDiscounts = visitDiscounts; }
    public BigDecimal getGrossSales() { return grossSales; }
    public void setGrossSales(BigDecimal grossSales) { this.grossSales = grossSales; }
    public BigDecimal getServiceStaffCommissions() { return serviceStaffCommissions; }
    public void setServiceStaffCommissions(BigDecimal serviceStaffCommissions) { this.serviceStaffCommissions = serviceStaffCommissions; }
    public BigDecimal getSaleStaffCommissions() { return saleStaffCommissions; }
    public void setSaleStaffCommissions(BigDecimal saleStaffCommissions) { this.saleStaffCommissions = saleStaffCommissions; }
    public BigDecimal getSalaries() { return salaries; }
    public void setSalaries(BigDecimal salaries) { this.salaries = salaries; }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }
    public BigDecimal getContributionMargin() { return contributionMargin; }
    public void setContributionMargin(BigDecimal contributionMargin) { this.contributionMargin = contributionMargin; }
    public BigDecimal getOperatingExpenses() { return operatingExpenses; }
    public void setOperatingExpenses(BigDecimal operatingExpenses) { this.operatingExpenses = operatingExpenses; }
    public BigDecimal getRealProfit() { return realProfit; }
    public void setRealProfit(BigDecimal realProfit) { this.realProfit = realProfit; }
    public List<ProfitMovementVo> getMovements() { return movements; }
    public void setMovements(List<ProfitMovementVo> movements) { this.movements = movements; }
    public List<ProfitPayrollVo> getPayroll() { return payroll; }
    public void setPayroll(List<ProfitPayrollVo> payroll) { this.payroll = payroll; }
    public List<ProfitDayVo> getDays() { return days; }
    public void setDays(List<ProfitDayVo> days) { this.days = days; }

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

    public BigDecimal getStaffCommissions() {
        return staffCommissions;
    }

    public void setStaffCommissions(BigDecimal staffCommissions) {
        this.staffCommissions = staffCommissions;
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

    public BigDecimal getBusinessExpenses() {
        return businessExpenses;
    }

    public void setBusinessExpenses(BigDecimal businessExpenses) {
        this.businessExpenses = businessExpenses;
    }

    public BigDecimal getProfitAfterExpenses() {
        return profitAfterExpenses;
    }

    public void setProfitAfterExpenses(BigDecimal profitAfterExpenses) {
        this.profitAfterExpenses = profitAfterExpenses;
    }

    public java.util.Map<String, BigDecimal> getBusinessExpensesByCategory() {
        return businessExpensesByCategory;
    }

    public void setBusinessExpensesByCategory(java.util.Map<String, BigDecimal> businessExpensesByCategory) {
        this.businessExpensesByCategory = businessExpensesByCategory;
    }

    public BigDecimal getPosRevenue() { return posRevenue; }
    public void setPosRevenue(BigDecimal posRevenue) { this.posRevenue = posRevenue; }
    public BigDecimal getVisitServices() { return visitServices; }
    public void setVisitServices(BigDecimal visitServices) { this.visitServices = visitServices; }
    public BigDecimal getVisitProducts() { return visitProducts; }
    public void setVisitProducts(BigDecimal visitProducts) { this.visitProducts = visitProducts; }
    public int getVisitsCount() { return visitsCount; }
    public void setVisitsCount(int visitsCount) { this.visitsCount = visitsCount; }

    public BigDecimal getConsignmentRevenue() {
        return consignmentRevenue;
    }

    public void setConsignmentRevenue(BigDecimal consignmentRevenue) {
        this.consignmentRevenue = consignmentRevenue;
    }

    public BigDecimal getConsignmentCommissions() {
        return consignmentCommissions;
    }

    public void setConsignmentCommissions(BigDecimal consignmentCommissions) {
        this.consignmentCommissions = consignmentCommissions;
    }

    public int getConsignmentCount() {
        return consignmentCount;
    }

    public void setConsignmentCount(int consignmentCount) {
        this.consignmentCount = consignmentCount;
    }
}
