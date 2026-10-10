package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Request to log a message sent to a customer.
 */
public class MessageLogRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer customerId;

    private Integer appointmentId;

    private String templateType;

    private String phone;

    private String body;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
