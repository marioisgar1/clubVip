package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data sent by the business to close (attend) an appointment.
 */
public class CloseAppointmentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String postNotes;

    private String recommendation;

    private String paymentMethod;

    /** Tip: PERCENT (tipValue = % of what the customer pays) or AMOUNT (tipValue = money); empty/0 = no tip. */
    private String tipMode;

    private java.math.BigDecimal tipValue;

    /** How the tip was paid (CASH / CARD / TRANSFER); empty = same as the visit. */
    private String tipPaymentMethod;

    private Boolean applyReward;

    private List<AppointmentItemVo> items = new ArrayList<>();

    /** True: close every open appointment of the visit in one charge (web agenda); false/null: this one only. */
    private Boolean wholeVisit;

    /** Appointments of the visit that were not performed: cancelled, not charged. */
    private List<Integer> skippedAppointmentIds = new ArrayList<>();

    /** Services added at the counter: each becomes an appointment of the visit, done and charged now. */
    private List<Integer> extraServiceIds = new ArrayList<>();

    /** When the visit really happened (yyyy-MM-dd, HH:mm, HH:mm); empty = it ends now. */
    private String visitDate;

    private String visitStart;

    private String visitEnd;

    public String getPostNotes() {
        return postNotes;
    }

    public void setPostNotes(String postNotes) {
        this.postNotes = postNotes;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getApplyReward() {
        return applyReward;
    }

    public void setApplyReward(Boolean applyReward) {
        this.applyReward = applyReward;
    }

    public List<AppointmentItemVo> getItems() {
        return items;
    }

    public void setItems(List<AppointmentItemVo> items) {
        this.items = items;
    }

    public Boolean getWholeVisit() {
        return wholeVisit;
    }

    public void setWholeVisit(Boolean wholeVisit) {
        this.wholeVisit = wholeVisit;
    }

    public List<Integer> getSkippedAppointmentIds() {
        return skippedAppointmentIds;
    }

    public void setSkippedAppointmentIds(List<Integer> skippedAppointmentIds) {
        this.skippedAppointmentIds = skippedAppointmentIds;
    }

    public List<Integer> getExtraServiceIds() {
        return extraServiceIds;
    }

    public void setExtraServiceIds(List<Integer> extraServiceIds) {
        this.extraServiceIds = extraServiceIds;
    }

    public String getTipMode() {
        return tipMode;
    }

    public void setTipMode(String tipMode) {
        this.tipMode = tipMode;
    }

    public java.math.BigDecimal getTipValue() {
        return tipValue;
    }

    public void setTipValue(java.math.BigDecimal tipValue) {
        this.tipValue = tipValue;
    }

    public String getTipPaymentMethod() {
        return tipPaymentMethod;
    }

    public void setTipPaymentMethod(String tipPaymentMethod) {
        this.tipPaymentMethod = tipPaymentMethod;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getVisitStart() {
        return visitStart;
    }

    public void setVisitStart(String visitStart) {
        this.visitStart = visitStart;
    }

    public String getVisitEnd() {
        return visitEnd;
    }

    public void setVisitEnd(String visitEnd) {
        this.visitEnd = visitEnd;
    }
}
