package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Bookable time slot with the employee that can take it.
 */
public class TimeSlotVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String startTime;

    private String endTime;

    private Integer employeeId;

    private String employeeName;

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

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
