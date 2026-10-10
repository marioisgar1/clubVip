package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Venue expense prorated per day. Dates travel as {@code yyyy-MM-dd}; {@code daysCount} and
 * {@code dailyCost} are computed by the server (inclusive days, totalCost / days, 4 decimals).
 */
public class LocationExpenseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer saleLocationId;

    private String saleLocationName;

    private String description;

    private String startDate;

    private String endDate;

    private Integer daysCount;

    private BigDecimal totalCost;

    private BigDecimal dailyCost;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSaleLocationId() {
        return saleLocationId;
    }

    public void setSaleLocationId(Integer saleLocationId) {
        this.saleLocationId = saleLocationId;
    }

    public String getSaleLocationName() {
        return saleLocationName;
    }

    public void setSaleLocationName(String saleLocationName) {
        this.saleLocationName = saleLocationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getDaysCount() {
        return daysCount;
    }

    public void setDaysCount(Integer daysCount) {
        this.daysCount = daysCount;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getDailyCost() {
        return dailyCost;
    }

    public void setDailyCost(BigDecimal dailyCost) {
        this.dailyCost = dailyCost;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
