package com.prolog.clubvip.vo;

import java.io.Serializable;

/** A page the browser must open (Stripe Checkout, billing portal). */
public class UrlVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
