package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Customer as seen by one business: visits, spending, loyalty and contact link.
 */
public class BusinessCustomerVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer customerId;

    private String fullName;

    private Integer age;

    private Integer birthDay;

    private Integer birthMonth;

    private String phone;

    private String email;

    private Date firstVisitAt;

    private Date lastVisitAt;

    private Integer visitsCount;

    private BigDecimal totalSpent;

    private BigDecimal averageTicket;

    private String notes;

    private Integer monthsInactive;

    private Boolean isNew;

    private Boolean birthdayToday;

    private Integer loyaltyStamps;

    private Integer loyaltyRequired;

    private Integer loyaltyPoints;

    private String waLink;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Integer birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFirstVisitAt() {
        return firstVisitAt;
    }

    public void setFirstVisitAt(Date firstVisitAt) {
        this.firstVisitAt = firstVisitAt;
    }

    public Date getLastVisitAt() {
        return lastVisitAt;
    }

    public void setLastVisitAt(Date lastVisitAt) {
        this.lastVisitAt = lastVisitAt;
    }

    public Integer getVisitsCount() {
        return visitsCount;
    }

    public void setVisitsCount(Integer visitsCount) {
        this.visitsCount = visitsCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public void setAverageTicket(BigDecimal averageTicket) {
        this.averageTicket = averageTicket;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getMonthsInactive() {
        return monthsInactive;
    }

    public void setMonthsInactive(Integer monthsInactive) {
        this.monthsInactive = monthsInactive;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public Boolean getBirthdayToday() {
        return birthdayToday;
    }

    public void setBirthdayToday(Boolean birthdayToday) {
        this.birthdayToday = birthdayToday;
    }

    public Integer getLoyaltyStamps() {
        return loyaltyStamps;
    }

    public void setLoyaltyStamps(Integer loyaltyStamps) {
        this.loyaltyStamps = loyaltyStamps;
    }

    public Integer getLoyaltyRequired() {
        return loyaltyRequired;
    }

    public void setLoyaltyRequired(Integer loyaltyRequired) {
        this.loyaltyRequired = loyaltyRequired;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getWaLink() {
        return waLink;
    }

    public void setWaLink(String waLink) {
        this.waLink = waLink;
    }
}
