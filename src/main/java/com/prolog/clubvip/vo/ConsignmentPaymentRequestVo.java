package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Consignación: the partner store pays some pending sold lines and attaches the receipt.
 */
public class ConsignmentPaymentRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Renglones pendientes que paga (saleItemId de ConsignmentSaleLineVo) */
    private List<Integer> saleItemIds = new ArrayList<>();
    /** Referencia, banco, etc. (opcional) */
    private String notes;
    /** Comprobante: base64 o data URL (imagen o PDF, máx. 8 MB) */
    private String receiptBase64;
    private String receiptName;
    private String receiptContentType;

    public List<Integer> getSaleItemIds() {
        return saleItemIds;
    }

    public void setSaleItemIds(List<Integer> saleItemIds) {
        this.saleItemIds = saleItemIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReceiptBase64() {
        return receiptBase64;
    }

    public void setReceiptBase64(String receiptBase64) {
        this.receiptBase64 = receiptBase64;
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
}
