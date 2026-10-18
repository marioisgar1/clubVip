package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Consignación: one line sold by the partner store (ficha → pestaña Ventas), with what the store keeps,
 * what the supplier gets and whether it is already paid. Sin datos del cliente.
 */
public class ConsignmentSaleLineVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer saleItemId;
    private Integer saleId;
    /** Folio de la venta en la tienda */
    private String folio;
    /** yyyy-MM-dd HH:mm, hora de México */
    private String saleDate;
    /** Producto del proveedor (origen de la copia) */
    private Integer productId;
    private String barcode;
    private String name;
    private String size;
    private String color;
    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();
    /** Con signo: en un cambio la prenda que regresa es negativa */
    private Integer quantity;
    private BigDecimal unitPrice;
    /** Lo cobrado por la tienda */
    private BigDecimal lineTotal;
    private BigDecimal commissionPercent;
    /** Lo que se queda la tienda */
    private BigDecimal commissionAmount;
    /** Lo que le toca al proveedor */
    private BigDecimal netAmount;
    /** Pago cerrado por el proveedor (CONFIRMED) */
    private boolean paid;
    /** Cuándo el proveedor lo cerró (yyyy-MM-dd HH:mm, hora de México) */
    private String paidAt;

    /** PENDING (sin pagar) | REPORTED (la tienda reportó el pago) | CONFIRMED (el proveedor lo cerró) */
    private String paymentStatus;
    /** Pago que lo cubre */
    private Integer paymentId;
    /** Cuándo la tienda reportó el pago (yyyy-MM-dd HH:mm) */
    private String reportedAt;

    public Integer getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(Integer saleItemId) {
        this.saleItemId = saleItemId;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
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
