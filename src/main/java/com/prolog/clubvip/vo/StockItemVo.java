package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Stock position of one product.
 */
public class StockItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;

    private String name;

    private String shortDescription;

    private String brand;

    /** SKU / barcode of the product. */
    private String barcode;

    private String familyName;

    private String size;

    private String color;

    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();

    /** Outlet (discontinued) product. */
    private Boolean isOutlet;

    /** Received on consignment: the supplier restocks it. */
    private Boolean consignment;

    private Integer quantityOnHand;

    private Integer minStock;

    private Boolean belowMinimum;

    private BigDecimal averageCost;

    private BigDecimal price;

    private BigDecimal stockValue;

    private BigDecimal marginPercent;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Boolean getBelowMinimum() {
        return belowMinimum;
    }

    public void setBelowMinimum(Boolean belowMinimum) {
        this.belowMinimum = belowMinimum;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public BigDecimal getMarginPercent() {
        return marginPercent;
    }

    public void setMarginPercent(BigDecimal marginPercent) {
        this.marginPercent = marginPercent;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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

    public Boolean getIsOutlet() {
        return isOutlet;
    }

    public void setIsOutlet(Boolean isOutlet) {
        this.isOutlet = isOutlet;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    /** Units missing to get back to the minimum (0 when it is exactly at the minimum). */
    /** Units to buy to get back to the minimum; a product never stays at zero, so the target is at least 1.
     *  A negative stock (oversold) counts as zero: the report asks for the same piece as an item at 0. */
    public int getShortage() {
        int min = minStock != null ? minStock : 0;
        int qty = Math.max(quantityOnHand != null ? quantityOnHand : 0, 0);
        return Math.max(Math.max(min, 1) - qty, 0);
    }

    /** Estimated cost of the shortage at the average cost. */
    public BigDecimal getShortageCost() {
        return (averageCost != null ? averageCost : BigDecimal.ZERO).multiply(BigDecimal.valueOf(getShortage()));
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
