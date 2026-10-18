package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Opening of a visit to a public section of a business (traffic/openVisit); the answer carries visitId. */
public class TrafficVisitVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer visitId;
    private Integer businessId;
    private String visitorKey;
    private Integer userId;
    /** WEB, APP, REWARDS. */
    private String source;
    /** PAGE, STORE, BOOKING. */
    private String section;
    private String device;
    private String referrer;
    private String userAgent;

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public String getVisitorKey() {
        return visitorKey;
    }

    public void setVisitorKey(String visitorKey) {
        this.visitorKey = visitorKey;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
