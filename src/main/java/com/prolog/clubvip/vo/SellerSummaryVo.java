package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Sales of one seller (owner, seller or employee) in a period, without detail: how many tickets,
 * what they brought in, what the goods and the card commissions cost, and what was left.
 */
public class SellerSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private int salesCount;
    /** Sum of subtotals of completed sales. */
    private BigDecimal revenue = BigDecimal.ZERO;
    /** Cost of the goods sold (unit cost at sale time). */
    private BigDecimal cogs = BigDecimal.ZERO;
    private BigDecimal commissions = BigDecimal.ZERO;
    /** Commission earned by the seller. */
    private BigDecimal staffCommission = BigDecimal.ZERO;
    /** revenue - cogs - commissions. */
    private BigDecimal profit = BigDecimal.ZERO;

    /** Negocio del vendedor: el propio o la tienda aliada */
    private String businessName;
    /** Renglón de ventas en consignación */
    private boolean consignment;
    /** Comisión de la tienda aliada */
    private BigDecimal consignmentCommission = BigDecimal.ZERO;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
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

    public BigDecimal getStaffCommission() {
        return staffCommission;
    }

    public void setStaffCommission(BigDecimal staffCommission) {
        this.staffCommission = staffCommission;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public boolean isConsignment() {
        return consignment;
    }

    public void setConsignment(boolean consignment) {
        this.consignment = consignment;
    }

    public BigDecimal getConsignmentCommission() {
        return consignmentCommission;
    }

    public void setConsignmentCommission(BigDecimal consignmentCommission) {
        this.consignmentCommission = consignmentCommission;
    }
}
