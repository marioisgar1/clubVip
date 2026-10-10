package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Occupancy of an employee: booked versus available minutes.
 */
public class OccupancyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer employeeId;

    private String employeeName;

    private String specialty;

    private Integer bookedMinutes;

    private Integer availableMinutes;

    private BigDecimal occupancyPercent;

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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Integer getBookedMinutes() {
        return bookedMinutes;
    }

    public void setBookedMinutes(Integer bookedMinutes) {
        this.bookedMinutes = bookedMinutes;
    }

    public Integer getAvailableMinutes() {
        return availableMinutes;
    }

    public void setAvailableMinutes(Integer availableMinutes) {
        this.availableMinutes = availableMinutes;
    }

    public BigDecimal getOccupancyPercent() {
        return occupancyPercent;
    }

    public void setOccupancyPercent(BigDecimal occupancyPercent) {
        this.occupancyPercent = occupancyPercent;
    }
}
