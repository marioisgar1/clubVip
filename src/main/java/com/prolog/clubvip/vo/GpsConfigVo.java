package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Report configuration of one device as the clubVipGps service keeps it (GET configs). */
public class GpsConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imei;
    private Integer profileId;
    private String profileName;
    private String status;
    private Integer attempts;
    private String requestedAt;
    private String sentAt;
    private String confirmedAt;
    private String lastResponse;
    private String lastError;
    private Boolean sendingEnabled;

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
    public String getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }
    public String getLastResponse() { return lastResponse; }
    public void setLastResponse(String lastResponse) { this.lastResponse = lastResponse; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Boolean getSendingEnabled() { return sendingEnabled; }
    public void setSendingEnabled(Boolean sendingEnabled) { this.sendingEnabled = sendingEnabled; }
}
