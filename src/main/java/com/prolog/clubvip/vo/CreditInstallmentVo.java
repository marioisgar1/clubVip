package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * One payment commitment. Carries the plan/customer context too, so the collection and
 * overdue-portfolio lists can render a row without a second lookup.
 */
public class CreditInstallmentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer creditPlanId;
    private Integer installmentNumber;

    /**
     * Plain day, never a timestamp: the clients edit it with a date picker and a timezone
     * shift would move a payment commitment to the wrong day.
     */
    private Date dueDate;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    /** PENDING | PARTIAL | PAID. */
    private String installmentStatus;
    private Date paidAt;

    /** True when it is still owed and the due date is already behind the reference date. */
    private Boolean overdue;
    /** Days past the due date; 0 when not overdue. */
    private Integer daysOverdue;
    /**
     * How late it was actually settled: days between the due date and the day the last
     * portion was collected. 0 when it was paid on time or is still pending.
     */
    private Integer daysLate;

    // --- context (portfolio / collection lists) ---
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    /** Where to go and collect; empty when the customer was saved without one. */
    private String customerAddress;
    private String saleFolio;
    private Integer saleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCreditPlanId() {
        return creditPlanId;
    }

    public void setCreditPlanId(Integer creditPlanId) {
        this.creditPlanId = creditPlanId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public String getInstallmentStatus() {
        return installmentStatus;
    }

    public void setInstallmentStatus(String installmentStatus) {
        this.installmentStatus = installmentStatus;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public Integer getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(Integer daysLate) {
        this.daysLate = daysLate;
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

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getSaleFolio() {
        return saleFolio;
    }

    public void setSaleFolio(String saleFolio) {
        this.saleFolio = saleFolio;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }
}
