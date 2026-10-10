package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class SaleItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer productId;
    private String barcode;
    private String description;
    private Integer quantity;       // signed on an exchange: +taken / -returned
    private BigDecimal listPrice;   // original retail price
    private BigDecimal unitPrice;   // real charged price (after haggling)
    private BigDecimal unitCost;    // moving-average cost at sale time
    private BigDecimal lineTotal;

    // --- exchange -----------------------------------------------------------
    /** Return line: the line of the original sale these units give back. */
    private Integer originSaleItemId;
    /** Return line: did the unit go back into sellable stock? */
    private Boolean restocked;
    private String returnReason;
    /** Original line: units already given back through previous exchanges. */
    private Integer returnedQuantity;
    /** Original line: units still exchangeable ({@code quantity - returnedQuantity}). */
    private Integer returnableQuantity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public void setListPrice(BigDecimal listPrice) {
        this.listPrice = listPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public Integer getOriginSaleItemId() {
        return originSaleItemId;
    }

    public void setOriginSaleItemId(Integer originSaleItemId) {
        this.originSaleItemId = originSaleItemId;
    }

    public Boolean getRestocked() {
        return restocked;
    }

    public void setRestocked(Boolean restocked) {
        this.restocked = restocked;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Integer getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(Integer returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public Integer getReturnableQuantity() {
        return returnableQuantity;
    }

    public void setReturnableQuantity(Integer returnableQuantity) {
        this.returnableQuantity = returnableQuantity;
    }

    // --- web-only helpers (no fields: the JSON contract stays identical to the backend) ---

    /** Convenience: quantity * unitPrice (computed on the fly). */
    public BigDecimal getComputedLineTotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Convenience: quantity * (listPrice - unitPrice), never negative. */
    public BigDecimal getComputedDiscount() {
        if (quantity == null || listPrice == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal diff = listPrice.subtract(unitPrice).multiply(BigDecimal.valueOf(quantity));
        return diff.signum() > 0 ? diff : BigDecimal.ZERO;
    }
}
