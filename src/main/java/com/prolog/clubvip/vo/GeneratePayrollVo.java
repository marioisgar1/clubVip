package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Request to generate a payroll period for an employee.
 */
public class GeneratePayrollVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer employeeId;

    private String periodStart;

    private String periodEnd;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }
}
