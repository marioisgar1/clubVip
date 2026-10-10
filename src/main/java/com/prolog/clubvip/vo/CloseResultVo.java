package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Result of closing an appointment: updated appointment, inventory, commission, loyalty and review request.
 */
public class CloseResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private AppointmentVo appointment;

    private Integer inventoryUnits;

    private BigDecimal commissionAmount;

    private Integer stampsAfter;

    private Integer stampsRequired;

    private Boolean rewardEarned;

    private Integer pointsAdded;

    private Boolean reviewRequested;

    private MessageComposeVo reviewMessage;

    public AppointmentVo getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentVo appointment) {
        this.appointment = appointment;
    }

    public Integer getInventoryUnits() {
        return inventoryUnits;
    }

    public void setInventoryUnits(Integer inventoryUnits) {
        this.inventoryUnits = inventoryUnits;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Integer getStampsAfter() {
        return stampsAfter;
    }

    public void setStampsAfter(Integer stampsAfter) {
        this.stampsAfter = stampsAfter;
    }

    public Integer getStampsRequired() {
        return stampsRequired;
    }

    public void setStampsRequired(Integer stampsRequired) {
        this.stampsRequired = stampsRequired;
    }

    public Boolean getRewardEarned() {
        return rewardEarned;
    }

    public void setRewardEarned(Boolean rewardEarned) {
        this.rewardEarned = rewardEarned;
    }

    public Integer getPointsAdded() {
        return pointsAdded;
    }

    public void setPointsAdded(Integer pointsAdded) {
        this.pointsAdded = pointsAdded;
    }

    public Boolean getReviewRequested() {
        return reviewRequested;
    }

    public void setReviewRequested(Boolean reviewRequested) {
        this.reviewRequested = reviewRequested;
    }

    public MessageComposeVo getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(MessageComposeVo reviewMessage) {
        this.reviewMessage = reviewMessage;
    }
}
