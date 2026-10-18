package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** One product line within a sale, for the detailed sales report. */
public class SaleItemDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String barcode;
    private String description;
    private String size;
    private String color;
    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();
    private String familyName;
    private Integer quantity;
    private BigDecimal listPrice;
    private BigDecimal unitPrice;
    private BigDecimal discount;   // quantity * (listPrice - unitPrice)
    private BigDecimal lineTotal;  // quantity * unitPrice

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public java.util.List<AttributeVo> getAttributes() {
        return attributes;
    }

    public void setAttributes(java.util.List<AttributeVo> attributes) {
        this.attributes = attributes;
    }

    /** "Talla: M · Color: Negro" of the characteristics named by the category. */
    public String getAttributesText() {
        StringBuilder sb = new StringBuilder();
        for (AttributeVo a : attributes != null ? attributes : java.util.Collections.<AttributeVo>emptyList()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(a.getLabel()).append(": ").append(a.getValue());
        }
        return sb.toString();
    }
}
