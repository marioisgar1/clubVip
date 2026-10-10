package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * How well one customer keeps her payment commitments: what she has bought on credit, what
 * she still owes, and above all how punctual she has been settling each instalment.
 *
 * <p>Punctuality is measured on instalments that are already settled, comparing the day the
 * last portion was collected against the day it was due. Instalments still pending count
 * only through {@link #overdueCount} and {@link #currentMaxDaysOverdue}, so a customer who
 * always paid on time but is late right now shows both facts instead of one hiding the other.
 *
 * <p>{@code history} carries every instalment she has ever had, newest due date first.
 */
public class CustomerBehaviorVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** No settled instalment yet: there is nothing to judge her on. */
    public static final String RATING_NEW = "NEW";
    public static final String RATING_EXCELLENT = "EXCELLENT";
    public static final String RATING_GOOD = "GOOD";
    public static final String RATING_FAIR = "FAIR";
    public static final String RATING_RISKY = "RISKY";

    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;

    // --- what she has bought on credit ---
    private Integer plansTotal = 0;
    private Integer plansOpen = 0;
    private Integer plansSettled = 0;
    private BigDecimal totalFinanced = BigDecimal.ZERO;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    /** Still owed across her open plans. */
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal overdueAmount = BigDecimal.ZERO;

    // --- commitments ---
    private Integer installmentsTotal = 0;
    private Integer installmentsSettled = 0;
    private Integer installmentsPending = 0;
    /** Settled on or before the due date. */
    private Integer paidOnTime = 0;
    /** Settled after the due date. */
    private Integer paidLate = 0;
    /** Pending instalments already past due today. */
    private Integer overdueCount = 0;

    // --- punctuality ---
    /** Share of settled instalments paid on time, 0..100; null when nothing is settled. */
    private BigDecimal onTimePercent;
    /** Mean delay over every settled instalment, on-time ones counting as zero. */
    private BigDecimal avgDaysLate = BigDecimal.ZERO;
    /** Worst delay she has ever run up on a settled instalment. */
    private Integer maxDaysLate = 0;
    /** Worst delay she is running right now; 0 when she owes nothing overdue. */
    private Integer currentMaxDaysOverdue = 0;

    private String rating = RATING_NEW;

    private Date firstPlanDate;
    private Date lastPaymentDate;

    private List<CreditInstallmentVo> history = new ArrayList<>();

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public Integer getPlansTotal() {
        return plansTotal;
    }

    public void setPlansTotal(Integer plansTotal) {
        this.plansTotal = plansTotal;
    }

    public Integer getPlansOpen() {
        return plansOpen;
    }

    public void setPlansOpen(Integer plansOpen) {
        this.plansOpen = plansOpen;
    }

    public Integer getPlansSettled() {
        return plansSettled;
    }

    public void setPlansSettled(Integer plansSettled) {
        this.plansSettled = plansSettled;
    }

    public BigDecimal getTotalFinanced() {
        return totalFinanced;
    }

    public void setTotalFinanced(BigDecimal totalFinanced) {
        this.totalFinanced = totalFinanced;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Integer getInstallmentsTotal() {
        return installmentsTotal;
    }

    public void setInstallmentsTotal(Integer installmentsTotal) {
        this.installmentsTotal = installmentsTotal;
    }

    public Integer getInstallmentsSettled() {
        return installmentsSettled;
    }

    public void setInstallmentsSettled(Integer installmentsSettled) {
        this.installmentsSettled = installmentsSettled;
    }

    public Integer getInstallmentsPending() {
        return installmentsPending;
    }

    public void setInstallmentsPending(Integer installmentsPending) {
        this.installmentsPending = installmentsPending;
    }

    public Integer getPaidOnTime() {
        return paidOnTime;
    }

    public void setPaidOnTime(Integer paidOnTime) {
        this.paidOnTime = paidOnTime;
    }

    public Integer getPaidLate() {
        return paidLate;
    }

    public void setPaidLate(Integer paidLate) {
        this.paidLate = paidLate;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    public BigDecimal getOnTimePercent() {
        return onTimePercent;
    }

    public void setOnTimePercent(BigDecimal onTimePercent) {
        this.onTimePercent = onTimePercent;
    }

    public BigDecimal getAvgDaysLate() {
        return avgDaysLate;
    }

    public void setAvgDaysLate(BigDecimal avgDaysLate) {
        this.avgDaysLate = avgDaysLate;
    }

    public Integer getMaxDaysLate() {
        return maxDaysLate;
    }

    public void setMaxDaysLate(Integer maxDaysLate) {
        this.maxDaysLate = maxDaysLate;
    }

    public Integer getCurrentMaxDaysOverdue() {
        return currentMaxDaysOverdue;
    }

    public void setCurrentMaxDaysOverdue(Integer currentMaxDaysOverdue) {
        this.currentMaxDaysOverdue = currentMaxDaysOverdue;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Date getFirstPlanDate() {
        return firstPlanDate;
    }

    public void setFirstPlanDate(Date firstPlanDate) {
        this.firstPlanDate = firstPlanDate;
    }

    public Date getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(Date lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public List<CreditInstallmentVo> getHistory() {
        return history;
    }

    public void setHistory(List<CreditInstallmentVo> history) {
        this.history = history;
    }
}
