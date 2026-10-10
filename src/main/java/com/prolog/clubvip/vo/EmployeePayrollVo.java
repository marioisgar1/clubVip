package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll view of an employee: profile, current period and history.
 */
public class EmployeePayrollVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmployeeVo employee;

    private PayrollPeriodVo current;

    private List<PayrollPeriodVo> periods = new ArrayList<>();

    public EmployeeVo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeVo employee) {
        this.employee = employee;
    }

    public PayrollPeriodVo getCurrent() {
        return current;
    }

    public void setCurrent(PayrollPeriodVo current) {
        this.current = current;
    }

    public List<PayrollPeriodVo> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PayrollPeriodVo> periods) {
        this.periods = periods;
    }
}
