package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Message ready to send: resolved body and WhatsApp link for a customer.
 */
public class MessageComposeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String templateType;

    private Integer customerId;

    private String customerName;

    private String phone;

    private String body;

    private String waLink;

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
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

    public String getWaLink() {
        return waLink;
    }

    public void setWaLink(String waLink) {
        this.waLink = waLink;
    }
}
