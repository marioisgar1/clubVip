package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A collection. Used both as the request of {@code credit/registerPayment} and as the
 * history rows of a plan.
 *
 * <p>Request rules: {@code creditInstallmentId} null means "the customer just handed me
 * this money", and the amount is applied to the oldest pending instalments; a null
 * {@code amount} together with an instalment means "she paid that instalment in full".
 */
public class CreditPaymentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer creditPlanId;

    private Integer creditInstallmentId;
    private Integer installmentNumber;
    private Date paymentDate;
    private BigDecimal amount;
    private Integer paymentMethodId;
    private String paymentMethodName;
    private String notes;

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

    public Integer getCreditInstallmentId() {
        return creditInstallmentId;
    }

    public void setCreditInstallmentId(Integer creditInstallmentId) {
        this.creditInstallmentId = creditInstallmentId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
