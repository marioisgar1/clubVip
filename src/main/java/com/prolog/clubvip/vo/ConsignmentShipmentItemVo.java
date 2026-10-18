package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One product line of a consignment shipment.
 */
public class ConsignmentShipmentItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer productId;

    private String barcode;

    private String name;

    private String size;

    private String color;

    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();

    private Integer quantitySent;

    private Integer quantityReceived;

    private BigDecimal unitPrice;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getQuantitySent() {
        return quantitySent;
    }

    public void setQuantitySent(Integer quantitySent) {
        this.quantitySent = quantitySent;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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
