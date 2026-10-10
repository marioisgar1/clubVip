package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String folio;
    /** SALE | EXCHANGE. */
    private String saleType;
    /** EXCHANGE only: sale whose goods were brought back. */
    private Integer originSaleId;
    private String originFolio;
    private Date saleDate;
    private Integer userId;

    /** Buyer. Optional on a cash ticket, required when the payment method is the credit one. */
    private Integer customerId;
    private String customerName;
    private String customerPhone;

    private Integer saleLocationId;
    private String saleLocationName;

    private Integer paymentMethodId;
    private String paymentMethodName;

    private BigDecimal subtotal;
    /** EXCHANGE only: value credited for the goods brought back. */
    private BigDecimal creditAmount;
    /** EXCHANGE only: credit surplus the customer did not use and was not refunded. */
    private BigDecimal forfeitedCredit;
    private BigDecimal commissionPercent;
    private BigDecimal commissionAmount;
    private BigDecimal total;
    private String saleStatus;
    private String cancelledReason;

    private List<SaleItemVo> items = new ArrayList<>();

    /** Present when some line was oversold (stock left negative); empty/null otherwise. */
    private List<SaleStockWarningVo> stockWarnings = new ArrayList<>();

    /**
     * Instalment plan. Sent in when the chosen payment method is the credit one, and
     * echoed back (already saved, with ids and due dates) so the client can build the
     * receipt, the PDF and the WhatsApp message right away.
     */
    private CreditPlanVo credit;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public Integer getOriginSaleId() {
        return originSaleId;
    }

    public void setOriginSaleId(Integer originSaleId) {
        this.originSaleId = originSaleId;
    }

    public String getOriginFolio() {
        return originFolio;
    }

    public void setOriginFolio(String originFolio) {
        this.originFolio = originFolio;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Integer getSaleLocationId() {
        return saleLocationId;
    }

    public void setSaleLocationId(Integer saleLocationId) {
        this.saleLocationId = saleLocationId;
    }

    public String getSaleLocationName() {
        return saleLocationName;
    }

    public void setSaleLocationName(String saleLocationName) {
        this.saleLocationName = saleLocationName;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getForfeitedCredit() {
        return forfeitedCredit;
    }

    public void setForfeitedCredit(BigDecimal forfeitedCredit) {
        this.forfeitedCredit = forfeitedCredit;
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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }

    public List<SaleItemVo> getItems() {
        return items;
    }

    public void setItems(List<SaleItemVo> items) {
        this.items = items;
    }

    public List<SaleStockWarningVo> getStockWarnings() {
        return stockWarnings;
    }

    public void setStockWarnings(List<SaleStockWarningVo> stockWarnings) {
        this.stockWarnings = stockWarnings;
    }

    public CreditPlanVo getCredit() {
        return credit;
    }

    public void setCredit(CreditPlanVo credit) {
        this.credit = credit;
    }
}
