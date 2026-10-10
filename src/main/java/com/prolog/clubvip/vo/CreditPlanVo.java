package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Instalment plan of a credit sale.
 *
 * <p>On the way IN (inside {@code SaleVo.credit} when registering a sale) only
 * {@code downPayment}, {@code installmentCount}, {@code frequency}, {@code firstDueDate}
 * and optionally {@code installments} matter — the seller may have moved the proposed
 * due dates or amounts, and whatever comes in {@code installments} wins.
 *
 * <p>On the way OUT it also carries the sale lines and the collection state, which is
 * everything the ticket, the PDF and the WhatsApp message need.
 */
public class CreditPlanVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer saleId;
    private String saleFolio;
    private Date saleDate;

    private Integer customerId;
    private String customerName;
    private String customerPhone;
    /** So a collection round can be walked straight off the list. */
    private String customerAddress;

    private Date planDate;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private BigDecimal financedAmount;
    private Integer installmentCount;
    /** WEEKLY | BIWEEKLY | MONTHLY. */
    private String frequency;

    /** Plain day; see {@link CreditInstallmentVo#getDueDate()}. */
    private Date firstDueDate;

    private BigDecimal paidAmount;
    private BigDecimal balance;
    /** OPEN | PAID | CANCELLED. */
    private String planStatus;
    private String notes;

    // --- collection state, computed against today ---
    private BigDecimal overdueAmount;
    private Integer overdueCount;
    private Date nextDueDate;
    private BigDecimal nextDueAmount;

    private List<CreditInstallmentVo> installments = new ArrayList<>();
    /** Sale lines, so the receipt can be rebuilt (detail view only). */
    private List<SaleItemVo> items = new ArrayList<>();
    /** Collection history (detail view only). */
    private List<CreditPaymentVo> payments = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public String getSaleFolio() {
        return saleFolio;
    }

    public void setSaleFolio(String saleFolio) {
        this.saleFolio = saleFolio;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
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

    public Date getPlanDate() {
        return planDate;
    }

    public void setPlanDate(Date planDate) {
        this.planDate = planDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public BigDecimal getFinancedAmount() {
        return financedAmount;
    }

    public void setFinancedAmount(BigDecimal financedAmount) {
        this.financedAmount = financedAmount;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Date getFirstDueDate() {
        return firstDueDate;
    }

    public void setFirstDueDate(Date firstDueDate) {
        this.firstDueDate = firstDueDate;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(Date nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public BigDecimal getNextDueAmount() {
        return nextDueAmount;
    }

    public void setNextDueAmount(BigDecimal nextDueAmount) {
        this.nextDueAmount = nextDueAmount;
    }

    public List<CreditInstallmentVo> getInstallments() {
        return installments;
    }

    public void setInstallments(List<CreditInstallmentVo> installments) {
        this.installments = installments;
    }

    public List<SaleItemVo> getItems() {
        return items;
    }

    public void setItems(List<SaleItemVo> items) {
        this.items = items;
    }

    public List<CreditPaymentVo> getPayments() {
        return payments;
    }

    public void setPayments(List<CreditPaymentVo> payments) {
        this.payments = payments;
    }
}
