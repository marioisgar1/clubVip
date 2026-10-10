package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory dashboard totals for a business.
 */
public class InventorySummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal stockValue;

    private Integer unitsOnHand;

    private Integer productsWithStock;

    private Integer belowMinimumCount;

    private List<String> belowMinimumNames = new ArrayList<>();

    private Integer soldUnitsMonth;

    private BigDecimal soldAmountMonth;

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public Integer getUnitsOnHand() {
        return unitsOnHand;
    }

    public void setUnitsOnHand(Integer unitsOnHand) {
        this.unitsOnHand = unitsOnHand;
    }

    public Integer getProductsWithStock() {
        return productsWithStock;
    }

    public void setProductsWithStock(Integer productsWithStock) {
        this.productsWithStock = productsWithStock;
    }

    public Integer getBelowMinimumCount() {
        return belowMinimumCount;
    }

    public void setBelowMinimumCount(Integer belowMinimumCount) {
        this.belowMinimumCount = belowMinimumCount;
    }

    public List<String> getBelowMinimumNames() {
        return belowMinimumNames;
    }

    public void setBelowMinimumNames(List<String> belowMinimumNames) {
        this.belowMinimumNames = belowMinimumNames;
    }

    public Integer getSoldUnitsMonth() {
        return soldUnitsMonth;
    }

    public void setSoldUnitsMonth(Integer soldUnitsMonth) {
        this.soldUnitsMonth = soldUnitsMonth;
    }

    public BigDecimal getSoldAmountMonth() {
        return soldAmountMonth;
    }

    public void setSoldAmountMonth(BigDecimal soldAmountMonth) {
        this.soldAmountMonth = soldAmountMonth;
    }
}
