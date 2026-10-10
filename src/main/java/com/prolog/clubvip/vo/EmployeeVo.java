package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff member of a business: profile, schedule, pay scheme and current period figures. {@code password} is input only.
 */
public class EmployeeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean ownHours;

    /** Own hours per weekday; null with ownHours=false = follows the business hours. */
    private List<DayHoursVo> weeklyHours;

    private Integer id;

    private Integer businessId;

    private Integer userId;

    private String fullName;

    private String specialty;

    private String phone;

    private String photoBase64;

    private String payScheme;

    private BigDecimal commissionPercent;

    private BigDecimal salaryAmount;

    private String payFrequency;

    private String workStartTime;

    private String workEndTime;

    private Boolean bookable;

    private Boolean canEditSchedule;

    private List<Integer> daysOff = new ArrayList<>();

    private List<String> dateOffs = new ArrayList<>();

    private List<Integer> serviceIds = new ArrayList<>();


    private String email;

    private String password;

    private Integer periodServices;

    private BigDecimal periodAmount;

    private String periodStart;

    private String periodEnd;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getPayScheme() {
        return payScheme;
    }

    public void setPayScheme(String payScheme) {
        this.payScheme = payScheme;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public BigDecimal getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount) {
        this.salaryAmount = salaryAmount;
    }

    public String getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency;
    }

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

    public Boolean getBookable() {
        return bookable;
    }

    public void setBookable(Boolean bookable) {
        this.bookable = bookable;
    }

    public Boolean getCanEditSchedule() {
        return canEditSchedule;
    }

    public void setCanEditSchedule(Boolean canEditSchedule) {
        this.canEditSchedule = canEditSchedule;
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

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPeriodServices() {
        return periodServices;
    }

    public void setPeriodServices(Integer periodServices) {
        this.periodServices = periodServices;
    }

    public BigDecimal getPeriodAmount() {
        return periodAmount;
    }

    public void setPeriodAmount(BigDecimal periodAmount) {
        this.periodAmount = periodAmount;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<DayHoursVo> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(List<DayHoursVo> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Boolean getOwnHours() {
        return ownHours;
    }

    public void setOwnHours(Boolean ownHours) {
        this.ownHours = ownHours;
    }
}
