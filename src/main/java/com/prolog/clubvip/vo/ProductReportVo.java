package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Generic per-product report row. Used both for the "most consulted" report
 * (value = lookup count) and the "best selling" report (totalQuantity / totalAmount).
 */
public class ProductReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;

    private String barcode;

    private String description;

    private String size;

    private String color;

    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();

    private String familyName;

    private String brandName;

    private Long value;

    private Long totalQuantity;

    private BigDecimal totalAmount;

    public ProductReportVo() {
    }

    /**
     * Constructor expression for the most-consulted report.
     */
    public ProductReportVo(Integer productId, String barcode, String description,
                           String size, String color,
                           String familyName, String brandName, Long value) {
        this.productId = productId;
        this.barcode = barcode;
        this.description = description;
        this.size = size;
        this.color = color;
        this.familyName = familyName;
        this.brandName = brandName;
        this.value = value;
    }

    /**
     * Constructor expression for the best-selling report.
     */
    public ProductReportVo(Integer productId, String barcode, String description,
                           String size, String color,
                           String familyName, String brandName, Long totalQuantity, BigDecimal totalAmount) {
        this.productId = productId;
        this.barcode = barcode;
        this.description = description;
        this.size = size;
        this.color = color;
        this.familyName = familyName;
        this.brandName = brandName;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
