package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Invitation a store sends to a supplier: commission the store keeps and how often it pays.
 */
public class ConsignmentInviteVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer supplierBusinessId;

    private BigDecimal commissionPercent;

    private String paymentFrequency;

    private String message;

    public Integer getSupplierBusinessId() {
        return supplierBusinessId;
    }

    public void setSupplierBusinessId(Integer supplierBusinessId) {
        this.supplierBusinessId = supplierBusinessId;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
