package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Consignación: a payment the partner store reported to the supplier (ficha → Ventas → Pagos).
 */
public class ConsignmentPaymentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    /** Suma del neto de los renglones que cubre */
    private BigDecimal amount;
    /** REPORTED (la tienda pagó, falta que el proveedor lo cierre) | CONFIRMED (cerrado) | REJECTED */
    private String paymentStatus;
    /** Nota de la tienda */
    private String notes;
    /** yyyy-MM-dd HH:mm, hora de México */
    private String reportedAt;
    private String reportedByName;
    /** yyyy-MM-dd HH:mm, hora de México */
    private String confirmedAt;
    private String rejectionReason;
    /** Renglones vendidos que cubre */
    private int lineCount;
    /** Folios de las ventas que cubre, separados por coma */
    private String folios;
    /** Tiene comprobante (se baja con getPaymentReceipt) */
    private boolean hasReceipt;
    private String receiptName;
    private String receiptContentType;
    /** El proveedor puede cerrarlo o rechazarlo */
    private boolean canConfirm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getReportedByName() {
        return reportedByName;
    }

    public void setReportedByName(String reportedByName) {
        this.reportedByName = reportedByName;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public String getFolios() {
        return folios;
    }

    public void setFolios(String folios) {
        this.folios = folios;
    }

    public boolean isHasReceipt() {
        return hasReceipt;
    }

    public void setHasReceipt(boolean hasReceipt) {
        this.hasReceipt = hasReceipt;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getReceiptContentType() {
        return receiptContentType;
    }

    public void setReceiptContentType(String receiptContentType) {
        this.receiptContentType = receiptContentType;
    }

    public boolean isCanConfirm() {
        return canConfirm;
    }

    public void setCanConfirm(boolean canConfirm) {
        this.canConfirm = canConfirm;
    }
}
