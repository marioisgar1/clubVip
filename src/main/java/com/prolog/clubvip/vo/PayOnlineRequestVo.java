package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Online payment request for an appointment.
 */
public class PayOnlineRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer appointmentId;

    private String cardToken;

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }
}
