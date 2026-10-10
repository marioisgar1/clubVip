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

    private Boolean applyReward;

    private List<AppointmentItemVo> items = new ArrayList<>();

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
}
