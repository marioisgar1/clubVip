package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Hours of one weekday in the weekly schedule of a business or an employee.
 * {@code weekday}: ISO 1=Monday..7=Sunday; {@code open=false} = closed / rest day.
 * Times as "HH:mm"; the break applies to businesses only (optional).
 */
public class DayHoursVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer weekday;
    private Boolean open;
    private String startTime;
    private String endTime;
    private String breakStartTime;
    private String breakEndTime;

    public DayHoursVo() {
    }

    public DayHoursVo(Integer weekday, Boolean open, String startTime, String endTime) {
        this.weekday = weekday;
        this.open = open;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getWeekday() {
        return weekday;
    }

    public void setWeekday(Integer weekday) {
        this.weekday = weekday;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(String breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public String getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(String breakEndTime) {
        this.breakEndTime = breakEndTime;
    }
}
