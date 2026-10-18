package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * What one employee cost the business in the report range: commissions on services and on sales plus
 * the fixed salary accrued day by day ({@code salaryDays}), next to the revenue she generated.
 */
public class ProfitPayrollVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer employeeId;
    private String employeeName;
    /** COMMISSION, SALARY or MIXED (current setting). */
    private String payScheme;
    private String payFrequency;
    /** Revenue of the visits she attended and the tickets she sold. */
    private BigDecimal revenue = BigDecimal.ZERO;
    private int operations;
    private BigDecimal serviceCommissions = BigDecimal.ZERO;
    private BigDecimal saleCommissions = BigDecimal.ZERO;
    /** Fixed salary accrued in the range (payroll cuts first, current salary for the rest). */
    private BigDecimal salary = BigDecimal.ZERO;
    private int salaryDays;
    private BigDecimal total = BigDecimal.ZERO;

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getPayScheme() { return payScheme; }
    public void setPayScheme(String payScheme) { this.payScheme = payScheme; }
    public String getPayFrequency() { return payFrequency; }
    public void setPayFrequency(String payFrequency) { this.payFrequency = payFrequency; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public int getOperations() { return operations; }
    public void setOperations(int operations) { this.operations = operations; }
    public BigDecimal getServiceCommissions() { return serviceCommissions; }
    public void setServiceCommissions(BigDecimal serviceCommissions) { this.serviceCommissions = serviceCommissions; }
    public BigDecimal getSaleCommissions() { return saleCommissions; }
    public void setSaleCommissions(BigDecimal saleCommissions) { this.saleCommissions = saleCommissions; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public int getSalaryDays() { return salaryDays; }
    public void setSalaryDays(int salaryDays) { this.salaryDays = salaryDays; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
