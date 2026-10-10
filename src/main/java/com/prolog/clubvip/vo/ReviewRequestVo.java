package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Review submitted by a customer for an appointment.
 */
public class ReviewRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer appointmentId;

    private Integer rating;

    private String comment;

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
