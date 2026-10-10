package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** One sale (header + its item breakdown) for the detailed sales report. */
public class SaleDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String folio;
    private Long saleDateMillis;    // epoch millis (UTC instant); the app formats it in the phone tz
    private String paymentMethod;
    private String location;

    private BigDecimal subtotal;          // sum of line totals (charged)
    private BigDecimal discount;          // sum of item discounts
    private BigDecimal commissionAmount;  // card/method commission for the whole sale
    private BigDecimal total;             // what the customer pays
    private BigDecimal cogs;              // cost of goods for this sale
    private BigDecimal profit;            // subtotal - cogs - commission

    private List<SaleItemDetailVo> items = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Long getSaleDateMillis() {
        return saleDateMillis;
    }

    public void setSaleDateMillis(Long saleDateMillis) {
        this.saleDateMillis = saleDateMillis;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getCogs() {
        return cogs;
    }

    public void setCogs(BigDecimal cogs) {
        this.cogs = cogs;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public List<SaleItemDetailVo> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDetailVo> items) {
        this.items = items;
    }
}
