package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Membership of the user's business: status, plan and dates, plus the two prices to show on the
 * "Activa tu membresía" screen. {@code required} = the business cannot work until it pays.
 */
public class BusinessMembershipVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private String plan;
    /** yyyy-MM-dd, null = no expiry (courtesy). */
    private String until;
    private boolean required;
    private boolean pastDue;
    /** Last day of the grace period while a failed renewal is retried (yyyy-MM-dd). */
    private String graceUntil;
    private boolean canManage;
    /** Grace days left while pastDue (0 = last day); -1 otherwise. */
    private int daysLeft = -1;
    /** OPEN | WON | LOST | REFUNDED when a chargeback or refund touched the membership. */
    private String disputeStatus;
    private String currency;
    private BigDecimal monthlyPrice;
    private BigDecimal annualPrice;
    /** What a year costs at the monthly price minus the annual price (the "one month free"). */
    private BigDecimal annualSaving;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isPastDue() {
        return pastDue;
    }

    public void setPastDue(boolean pastDue) {
        this.pastDue = pastDue;
    }

    public String getGraceUntil() {
        return graceUntil;
    }

    public void setGraceUntil(String graceUntil) {
        this.graceUntil = graceUntil;
    }

    public boolean isCanManage() {
        return canManage;
    }

    public void setCanManage(boolean canManage) {
        this.canManage = canManage;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getAnnualPrice() {
        return annualPrice;
    }

    public void setAnnualPrice(BigDecimal annualPrice) {
        this.annualPrice = annualPrice;
    }

    public BigDecimal getAnnualSaving() {
        return annualSaving;
    }

    public void setAnnualSaving(BigDecimal annualSaving) {
        this.annualSaving = annualSaving;
    }

    public String getDisputeStatus() {
        return disputeStatus;
    }

    public void setDisputeStatus(String disputeStatus) {
        this.disputeStatus = disputeStatus;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }
}
