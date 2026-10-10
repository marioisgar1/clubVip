package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Public contact of a business: phone and WhatsApp link.
 */
public class ContactLinkVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String phone;

    private String waLink;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWaLink() {
        return waLink;
    }

    public void setWaLink(String waLink) {
        this.waLink = waLink;
    }
}
