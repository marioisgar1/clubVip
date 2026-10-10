package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Consignment relationship seen from the caller's business: role is the caller's side
 * (SUPPLIER owns the products, STORE sells them) and partner is the other business.
 */
public class ConsignmentAgreementVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String role;

    private BusinessSummaryVo partner;

    private BigDecimal commissionPercent;

    private BigDecimal supplierPercent;

    private String paymentFrequency;

    private String invitationMessage;

    private String agreementStatus;

    private String rejectionReason;

    private String invitedAt;

    private String respondedAt;

    private Boolean canAnswer;

    private Boolean canCancel;

    private Integer piecesAtStore;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BusinessSummaryVo getPartner() {
        return partner;
    }

    public void setPartner(BusinessSummaryVo partner) {
        this.partner = partner;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public BigDecimal getSupplierPercent() {
        return supplierPercent;
    }

    public void setSupplierPercent(BigDecimal supplierPercent) {
        this.supplierPercent = supplierPercent;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public String getInvitationMessage() {
        return invitationMessage;
    }

    public void setInvitationMessage(String invitationMessage) {
        this.invitationMessage = invitationMessage;
    }

    public String getAgreementStatus() {
        return agreementStatus;
    }

    public void setAgreementStatus(String agreementStatus) {
        this.agreementStatus = agreementStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(String invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(String respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Boolean getCanAnswer() {
        return canAnswer;
    }

    public void setCanAnswer(Boolean canAnswer) {
        this.canAnswer = canAnswer;
    }

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public Integer getPiecesAtStore() {
        return piecesAtStore;
    }

    public void setPiecesAtStore(Integer piecesAtStore) {
        this.piecesAtStore = piecesAtStore;
    }
}
