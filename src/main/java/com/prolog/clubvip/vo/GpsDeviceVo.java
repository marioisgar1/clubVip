package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Status of one GPS device (clubVipGps /devices). Dates are ISO-8601 text (UTC); status ONLINE / SILENT / OFFLINE. */
public class GpsDeviceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imei;
    private String model;
    private String swVersion;
    private String status;
    private Boolean connected;
    private String remoteAddress;
    private String connectedAt;
    private Long connectedForSeconds;
    private String disconnectedAt;
    private Long disconnectedForSeconds;
    private String disconnectedForText;
    private String lastFrameAt;
    private Long secondsSinceLastFrame;
    private String sinceLastFrameText;
    private String lastHeader;
    private String lastGpsTime;
    private Double lastLatitude;
    private Double lastLongitude;
    private Double lastSpeed;
    private Long framesTotal;
    private Long framesSession;

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSwVersion() { return swVersion; }
    public void setSwVersion(String swVersion) { this.swVersion = swVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getConnected() { return connected; }
    public void setConnected(Boolean connected) { this.connected = connected; }
    public String getRemoteAddress() { return remoteAddress; }
    public void setRemoteAddress(String remoteAddress) { this.remoteAddress = remoteAddress; }
    public String getConnectedAt() { return connectedAt; }
    public void setConnectedAt(String connectedAt) { this.connectedAt = connectedAt; }
    public Long getConnectedForSeconds() { return connectedForSeconds; }
    public void setConnectedForSeconds(Long connectedForSeconds) { this.connectedForSeconds = connectedForSeconds; }
    public String getDisconnectedAt() { return disconnectedAt; }
    public void setDisconnectedAt(String disconnectedAt) { this.disconnectedAt = disconnectedAt; }
    public Long getDisconnectedForSeconds() { return disconnectedForSeconds; }
    public void setDisconnectedForSeconds(Long disconnectedForSeconds) { this.disconnectedForSeconds = disconnectedForSeconds; }
    public String getDisconnectedForText() { return disconnectedForText; }
    public void setDisconnectedForText(String disconnectedForText) { this.disconnectedForText = disconnectedForText; }
    public String getLastFrameAt() { return lastFrameAt; }
    public void setLastFrameAt(String lastFrameAt) { this.lastFrameAt = lastFrameAt; }
    public Long getSecondsSinceLastFrame() { return secondsSinceLastFrame; }
    public void setSecondsSinceLastFrame(Long secondsSinceLastFrame) { this.secondsSinceLastFrame = secondsSinceLastFrame; }
    public String getSinceLastFrameText() { return sinceLastFrameText; }
    public void setSinceLastFrameText(String sinceLastFrameText) { this.sinceLastFrameText = sinceLastFrameText; }
    public String getLastHeader() { return lastHeader; }
    public void setLastHeader(String lastHeader) { this.lastHeader = lastHeader; }
    public String getLastGpsTime() { return lastGpsTime; }
    public void setLastGpsTime(String lastGpsTime) { this.lastGpsTime = lastGpsTime; }
    public Double getLastLatitude() { return lastLatitude; }
    public void setLastLatitude(Double lastLatitude) { this.lastLatitude = lastLatitude; }
    public Double getLastLongitude() { return lastLongitude; }
    public void setLastLongitude(Double lastLongitude) { this.lastLongitude = lastLongitude; }
    public Double getLastSpeed() { return lastSpeed; }
    public void setLastSpeed(Double lastSpeed) { this.lastSpeed = lastSpeed; }
    public Long getFramesTotal() { return framesTotal; }
    public void setFramesTotal(Long framesTotal) { this.framesTotal = framesTotal; }
    public Long getFramesSession() { return framesSession; }
    public void setFramesSession(Long framesSession) { this.framesSession = framesSession; }
}
