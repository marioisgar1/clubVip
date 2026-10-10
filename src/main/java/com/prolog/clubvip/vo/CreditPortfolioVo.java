package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * "How much do they owe me and what has to be collected today", as of a reference date.
 *
 * <p>{@code lines} are every unpaid instalment of every live plan, oldest first, so the
 * screen can show the overdue ones at the top and still let the seller look ahead.
 */
public class CreditPortfolioVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date asOf;

    /** Everything still owed across all live plans. */
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    private BigDecimal overdueAmount = BigDecimal.ZERO;
    private Integer overdueCount = 0;
    private BigDecimal dueTodayAmount = BigDecimal.ZERO;
    private Integer dueTodayCount = 0;
    private BigDecimal upcomingAmount = BigDecimal.ZERO;
    private Integer upcomingCount = 0;

    /** Distinct customers with something still owed. */
    private Integer customersCount = 0;
    /** Distinct customers with at least one overdue instalment. */
    private Integer overdueCustomersCount = 0;

    private List<CreditInstallmentVo> lines = new ArrayList<>();

    public Date getAsOf() {
        return asOf;
    }

    public void setAsOf(Date asOf) {
        this.asOf = asOf;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    public BigDecimal getDueTodayAmount() {
        return dueTodayAmount;
    }

    public void setDueTodayAmount(BigDecimal dueTodayAmount) {
        this.dueTodayAmount = dueTodayAmount;
    }

    public Integer getDueTodayCount() {
        return dueTodayCount;
    }

    public void setDueTodayCount(Integer dueTodayCount) {
        this.dueTodayCount = dueTodayCount;
    }

    public BigDecimal getUpcomingAmount() {
        return upcomingAmount;
    }

    public void setUpcomingAmount(BigDecimal upcomingAmount) {
        this.upcomingAmount = upcomingAmount;
    }

    public Integer getUpcomingCount() {
        return upcomingCount;
    }

    public void setUpcomingCount(Integer upcomingCount) {
        this.upcomingCount = upcomingCount;
    }

    public Integer getCustomersCount() {
        return customersCount;
    }

    public void setCustomersCount(Integer customersCount) {
        this.customersCount = customersCount;
    }

    public Integer getOverdueCustomersCount() {
        return overdueCustomersCount;
    }

    public void setOverdueCustomersCount(Integer overdueCustomersCount) {
        this.overdueCustomersCount = overdueCustomersCount;
    }

    public List<CreditInstallmentVo> getLines() {
        return lines;
    }

    public void setLines(List<CreditInstallmentVo> lines) {
        this.lines = lines;
    }
}
