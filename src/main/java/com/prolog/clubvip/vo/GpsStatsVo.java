package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** Summary of the GPS receiver (clubVipGps /stats). */
public class GpsStatsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String now;
    private String startedAt;
    private Long uptimeSeconds;
    private Integer devicesKnown;
    private Integer devicesOnline;
    private Integer devicesSilent;
    private Integer devicesOffline;
    private Long framesThisMinute;
    private Long framesLastMinute;
    private Double framesPerMinuteAvg;
    private Long framesPerMinuteMax;
    private Integer windowMinutes;
    private Long framesInWindow;
    private Long keepalivesInWindow;
    private Long errorsInWindow;
    private Long droppedInWindow;
    private Integer writeQueue;
    private Long totalFrames;
    private Long totalKeepalives;
    private Long totalErrors;
    private Long totalDropped;
    private Long totalConnections;
    private Map<String, Long> totalByHeader;
    private List<GpsMinuteVo> perMinute;

    public String getNow() { return now; }
    public void setNow(String now) { this.now = now; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public Long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(Long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
    public Integer getDevicesKnown() { return devicesKnown; }
    public void setDevicesKnown(Integer devicesKnown) { this.devicesKnown = devicesKnown; }
    public Integer getDevicesOnline() { return devicesOnline; }
    public void setDevicesOnline(Integer devicesOnline) { this.devicesOnline = devicesOnline; }
    public Integer getDevicesSilent() { return devicesSilent; }
    public void setDevicesSilent(Integer devicesSilent) { this.devicesSilent = devicesSilent; }
    public Integer getDevicesOffline() { return devicesOffline; }
    public void setDevicesOffline(Integer devicesOffline) { this.devicesOffline = devicesOffline; }
    public Long getFramesThisMinute() { return framesThisMinute; }
    public void setFramesThisMinute(Long framesThisMinute) { this.framesThisMinute = framesThisMinute; }
    public Long getFramesLastMinute() { return framesLastMinute; }
    public void setFramesLastMinute(Long framesLastMinute) { this.framesLastMinute = framesLastMinute; }
    public Double getFramesPerMinuteAvg() { return framesPerMinuteAvg; }
    public void setFramesPerMinuteAvg(Double framesPerMinuteAvg) { this.framesPerMinuteAvg = framesPerMinuteAvg; }
    public Long getFramesPerMinuteMax() { return framesPerMinuteMax; }
    public void setFramesPerMinuteMax(Long framesPerMinuteMax) { this.framesPerMinuteMax = framesPerMinuteMax; }
    public Integer getWindowMinutes() { return windowMinutes; }
    public void setWindowMinutes(Integer windowMinutes) { this.windowMinutes = windowMinutes; }
    public Long getFramesInWindow() { return framesInWindow; }
    public void setFramesInWindow(Long framesInWindow) { this.framesInWindow = framesInWindow; }
    public Long getKeepalivesInWindow() { return keepalivesInWindow; }
    public void setKeepalivesInWindow(Long keepalivesInWindow) { this.keepalivesInWindow = keepalivesInWindow; }
    public Long getErrorsInWindow() { return errorsInWindow; }
    public void setErrorsInWindow(Long errorsInWindow) { this.errorsInWindow = errorsInWindow; }
    public Long getDroppedInWindow() { return droppedInWindow; }
    public void setDroppedInWindow(Long droppedInWindow) { this.droppedInWindow = droppedInWindow; }
    public Integer getWriteQueue() { return writeQueue; }
    public void setWriteQueue(Integer writeQueue) { this.writeQueue = writeQueue; }
    public Long getTotalFrames() { return totalFrames; }
    public void setTotalFrames(Long totalFrames) { this.totalFrames = totalFrames; }
    public Long getTotalKeepalives() { return totalKeepalives; }
    public void setTotalKeepalives(Long totalKeepalives) { this.totalKeepalives = totalKeepalives; }
    public Long getTotalErrors() { return totalErrors; }
    public void setTotalErrors(Long totalErrors) { this.totalErrors = totalErrors; }
    public Long getTotalDropped() { return totalDropped; }
    public void setTotalDropped(Long totalDropped) { this.totalDropped = totalDropped; }
    public Long getTotalConnections() { return totalConnections; }
    public void setTotalConnections(Long totalConnections) { this.totalConnections = totalConnections; }
    public Map<String, Long> getTotalByHeader() { return totalByHeader; }
    public void setTotalByHeader(Map<String, Long> totalByHeader) { this.totalByHeader = totalByHeader; }
    public List<GpsMinuteVo> getPerMinute() { return perMinute; }
    public void setPerMinute(List<GpsMinuteVo> perMinute) { this.perMinute = perMinute; }
}
