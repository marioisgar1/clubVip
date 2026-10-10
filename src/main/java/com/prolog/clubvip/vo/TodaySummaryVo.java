package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Home dashboard of the business for today.
 */
public class TodaySummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private String businessName;

    private Boolean verified;

    private Integer appointmentsCount;

    private Integer attendedCount;

    private Integer pendingCount;

    private Integer cancelledCount;

    private BigDecimal revenue;

    private Integer newCustomersWeek;

    private Integer totalCustomers;

    private List<BusinessCustomerVo> birthdays = new ArrayList<>();

    private List<AppointmentVo> appointments = new ArrayList<>();

    private Integer inactiveCount;

    private Integer pendingReviews;

    private Integer belowMinimumProducts;

    private Integer pendingPayrolls;

    private BigDecimal ratingAverage;

    private Integer ratingCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Integer getAppointmentsCount() {
        return appointmentsCount;
    }

    public void setAppointmentsCount(Integer appointmentsCount) {
        this.appointmentsCount = appointmentsCount;
    }

    public Integer getAttendedCount() {
        return attendedCount;
    }

    public void setAttendedCount(Integer attendedCount) {
        this.attendedCount = attendedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(Integer cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Integer getNewCustomersWeek() {
        return newCustomersWeek;
    }

    public void setNewCustomersWeek(Integer newCustomersWeek) {
        this.newCustomersWeek = newCustomersWeek;
    }

    public Integer getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Integer totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public List<BusinessCustomerVo> getBirthdays() {
        return birthdays;
    }

    public void setBirthdays(List<BusinessCustomerVo> birthdays) {
        this.birthdays = birthdays;
    }

    public List<AppointmentVo> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentVo> appointments) {
        this.appointments = appointments;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public void setInactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
    }

    public Integer getPendingReviews() {
        return pendingReviews;
    }

    public void setPendingReviews(Integer pendingReviews) {
        this.pendingReviews = pendingReviews;
    }

    public Integer getBelowMinimumProducts() {
        return belowMinimumProducts;
    }

    public void setBelowMinimumProducts(Integer belowMinimumProducts) {
        this.belowMinimumProducts = belowMinimumProducts;
    }

    public Integer getPendingPayrolls() {
        return pendingPayrolls;
    }

    public void setPendingPayrolls(Integer pendingPayrolls) {
        this.pendingPayrolls = pendingPayrolls;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(BigDecimal ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
}
