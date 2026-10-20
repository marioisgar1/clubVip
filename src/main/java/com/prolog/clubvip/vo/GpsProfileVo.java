package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Report profile of the GPS devices (backend gpsProfile/*); copy of the backend VO. */
public class GpsProfileVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String description;
    private Integer parkedIntervalSec;
    private Integer movingIntervalSec;
    private Integer distanceM;
    private Integer angleDeg;
    private Integer keepAliveMin;
    private Integer speedLimitKmh;
    private Integer idleAlertMin;
    private Boolean ignitionReport;
    private Boolean harshDriving;
    private Boolean sleepWhenParked;
    private Long vehicleCount;
    private Integer status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getParkedIntervalSec() { return parkedIntervalSec; }
    public void setParkedIntervalSec(Integer parkedIntervalSec) { this.parkedIntervalSec = parkedIntervalSec; }
    public Integer getMovingIntervalSec() { return movingIntervalSec; }
    public void setMovingIntervalSec(Integer movingIntervalSec) { this.movingIntervalSec = movingIntervalSec; }
    public Integer getDistanceM() { return distanceM; }
    public void setDistanceM(Integer distanceM) { this.distanceM = distanceM; }
    public Integer getAngleDeg() { return angleDeg; }
    public void setAngleDeg(Integer angleDeg) { this.angleDeg = angleDeg; }
    public Integer getKeepAliveMin() { return keepAliveMin; }
    public void setKeepAliveMin(Integer keepAliveMin) { this.keepAliveMin = keepAliveMin; }
    public Integer getSpeedLimitKmh() { return speedLimitKmh; }
    public void setSpeedLimitKmh(Integer speedLimitKmh) { this.speedLimitKmh = speedLimitKmh; }
    public Integer getIdleAlertMin() { return idleAlertMin; }
    public void setIdleAlertMin(Integer idleAlertMin) { this.idleAlertMin = idleAlertMin; }
    public Boolean getIgnitionReport() { return ignitionReport; }
    public void setIgnitionReport(Boolean ignitionReport) { this.ignitionReport = ignitionReport; }
    public Boolean getHarshDriving() { return harshDriving; }
    public void setHarshDriving(Boolean harshDriving) { this.harshDriving = harshDriving; }
    public Boolean getSleepWhenParked() { return sleepWhenParked; }
    public void setSleepWhenParked(Boolean sleepWhenParked) { this.sleepWhenParked = sleepWhenParked; }
    public Long getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(Long vehicleCount) { this.vehicleCount = vehicleCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
