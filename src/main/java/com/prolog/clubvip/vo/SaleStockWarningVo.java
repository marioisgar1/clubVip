package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Warning attached to a sale response when a line was sold beyond the available stock,
 * leaving the SKU at a negative quantity. Lets the app tell the operator which product's
 * inventory needs to be corrected. {@code resultingQuantity} is the on-hand value left
 * after the sale (negative).
 */
public class SaleStockWarningVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;
    private String barcode;
    private Integer resultingQuantity;

    public SaleStockWarningVo() {
    }

    public SaleStockWarningVo(String description, String barcode, Integer resultingQuantity) {
        this.description = description;
        this.barcode = barcode;
        this.resultingQuantity = resultingQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getResultingQuantity() {
        return resultingQuantity;
    }

    public void setResultingQuantity(Integer resultingQuantity) {
        this.resultingQuantity = resultingQuantity;
    }
}
