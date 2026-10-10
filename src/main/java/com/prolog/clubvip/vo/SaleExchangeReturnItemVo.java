package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** One line of the original sale the customer is bringing back in an exchange. */
public class SaleExchangeReturnItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Id of the {@code sale_item} row of the ORIGINAL sale (not the product id). */
    private Integer saleItemId;

    private Integer quantity;

    /**
     * Whether the unit goes back into sellable stock. False for damaged / used goods: the
     * customer still gets the credit but the cost stays consumed and stock is not touched.
     */
    private boolean restock = true;

    private String reason;

    // --- web-only display fields (transient: never sent, never compared with the backend) ---
    private transient String barcode;
    private transient String description;
    private transient BigDecimal unitPrice;
    private transient Integer returnableQuantity;

    public Integer getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(Integer saleItemId) {
        this.saleItemId = saleItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isRestock() {
        return restock;
    }

    public void setRestock(boolean restock) {
        this.restock = restock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getReturnableQuantity() {
        return returnableQuantity;
    }

    public void setReturnableQuantity(Integer returnableQuantity) {
        this.returnableQuantity = returnableQuantity;
    }

    /** Credit this line gives back = unitPrice * quantity. */
    public BigDecimal getComputedCredit() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
