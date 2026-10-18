package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Result of one calendar day of the profit report: income, every cost, and what was left. */
public class ProfitDayVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Start of the day in the business zone. */
    private Long dayMillis;
    private int operations;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal cogs = BigDecimal.ZERO;
    private BigDecimal fees = BigDecimal.ZERO;
    private BigDecimal staffCommissions = BigDecimal.ZERO;
    private BigDecimal salaries = BigDecimal.ZERO;
    private BigDecimal venueCost = BigDecimal.ZERO;
    private BigDecimal expenses = BigDecimal.ZERO;
    private BigDecimal net = BigDecimal.ZERO;

    /** Comisiones de tiendas aliadas del día */
    private BigDecimal consignmentCommissions = BigDecimal.ZERO;

    public Long getDayMillis() { return dayMillis; }
    public void setDayMillis(Long dayMillis) { this.dayMillis = dayMillis; }
    public int getOperations() { return operations; }
    public void setOperations(int operations) { this.operations = operations; }
    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }
    public BigDecimal getCogs() { return cogs; }
    public void setCogs(BigDecimal cogs) { this.cogs = cogs; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public BigDecimal getStaffCommissions() { return staffCommissions; }
    public void setStaffCommissions(BigDecimal staffCommissions) { this.staffCommissions = staffCommissions; }
    public BigDecimal getSalaries() { return salaries; }
    public void setSalaries(BigDecimal salaries) { this.salaries = salaries; }
    public BigDecimal getVenueCost() { return venueCost; }
    public void setVenueCost(BigDecimal venueCost) { this.venueCost = venueCost; }
    public BigDecimal getExpenses() { return expenses; }
    public void setExpenses(BigDecimal expenses) { this.expenses = expenses; }
    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }

    public BigDecimal getConsignmentCommissions() {
        return consignmentCommissions;
    }

    public void setConsignmentCommissions(BigDecimal consignmentCommissions) {
        this.consignmentCommissions = consignmentCommissions;
    }
}
