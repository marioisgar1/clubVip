package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Schedule update of an employee: working hours, weekly days off and specific dates off.
 */
public class ScheduleUpdateVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String workStartTime;

    private String workEndTime;

    private List<Integer> daysOff = new ArrayList<>();

    private List<String> dateOffs = new ArrayList<>();

    public String getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(String workStartTime) {
        this.workStartTime = workStartTime;
    }

    public String getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(String workEndTime) {
        this.workEndTime = workEndTime;
    }

    public List<Integer> getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(List<Integer> daysOff) {
        this.daysOff = daysOff;
    }

    public List<String> getDateOffs() {
        return dateOffs;
    }

    public void setDateOffs(List<String> dateOffs) {
        this.dateOffs = dateOffs;
    }
}
