package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Frames of one minute (clubVipGps /stats perMinute). */
public class GpsMinuteVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String minute;
    private Long frames;
    private Long keepalives;
    private Long errors;
    private Long dropped;
    private Long devicesReporting;
    private Long devicesConnected;

    public String getMinute() { return minute; }
    public void setMinute(String minute) { this.minute = minute; }
    public Long getFrames() { return frames; }
    public void setFrames(Long frames) { this.frames = frames; }
    public Long getKeepalives() { return keepalives; }
    public void setKeepalives(Long keepalives) { this.keepalives = keepalives; }
    public Long getErrors() { return errors; }
    public void setErrors(Long errors) { this.errors = errors; }
    public Long getDropped() { return dropped; }
    public void setDropped(Long dropped) { this.dropped = dropped; }
    public Long getDevicesReporting() { return devicesReporting; }
    public void setDevicesReporting(Long devicesReporting) { this.devicesReporting = devicesReporting; }
    public Long getDevicesConnected() { return devicesConnected; }
    public void setDevicesConnected(Long devicesConnected) { this.devicesConnected = devicesConnected; }
}
