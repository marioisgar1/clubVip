package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Appointment with business, customer, employee and service context, totals and status.
 */
public class AppointmentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer businessId;

    private String businessName;

    private String businessAddress;

    private String businessPhone;

    private Boolean businessVerified;

    private Integer customerId;

    private String customerName;

    private String customerPhone;

    private Integer customerAge;

    private String customerBirthday;

    private Integer customerVisits;

    private Date customerSince;

    private Integer employeeId;

    private String employeeName;

    private Integer serviceId;

    private String serviceName;

    private String date;

    private String startTime;

    private String endTime;

    private Integer durationMinutes;

    private BigDecimal servicePrice;

    private BigDecimal productsTotal;

    private BigDecimal rewardDiscount;

    private BigDecimal total;

    private String appointmentStatus;

    private String paymentMode;

    private String paymentStatus;

    private String paymentMethod;

    private String source;

    private String cancelledReason;

    private Date closedAt;

    private Date reminderSentAt;

    private String preNotes;

    private String postNotes;

    private String recommendation;

    private String previousPostNotes;

    private String previousVisitDate;

    private List<AppointmentItemVo> items = new ArrayList<>();

    private Boolean canReview;

    private Boolean reviewed;

    private Boolean rewardAvailable;

    private String waLink;

    private Date createdAt;

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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public Boolean getBusinessVerified() {
        return businessVerified;
    }

    public void setBusinessVerified(Boolean businessVerified) {
        this.businessVerified = businessVerified;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Integer getCustomerAge() {
        return customerAge;
    }

    public void setCustomerAge(Integer customerAge) {
        this.customerAge = customerAge;
    }

    public String getCustomerBirthday() {
        return customerBirthday;
    }

    public void setCustomerBirthday(String customerBirthday) {
        this.customerBirthday = customerBirthday;
    }

    public Integer getCustomerVisits() {
        return customerVisits;
    }

    public void setCustomerVisits(Integer customerVisits) {
        this.customerVisits = customerVisits;
    }

    public Date getCustomerSince() {
        return customerSince;
    }

    public void setCustomerSince(Date customerSince) {
        this.customerSince = customerSince;
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

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(BigDecimal servicePrice) {
        this.servicePrice = servicePrice;
    }

    public BigDecimal getProductsTotal() {
        return productsTotal;
    }

    public void setProductsTotal(BigDecimal productsTotal) {
        this.productsTotal = productsTotal;
    }

    public BigDecimal getRewardDiscount() {
        return rewardDiscount;
    }

    public void setRewardDiscount(BigDecimal rewardDiscount) {
        this.rewardDiscount = rewardDiscount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public Date getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(Date reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

    public String getPreNotes() {
        return preNotes;
    }

    public void setPreNotes(String preNotes) {
        this.preNotes = preNotes;
    }

    public String getPostNotes() {
        return postNotes;
    }

    public void setPostNotes(String postNotes) {
        this.postNotes = postNotes;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getPreviousPostNotes() {
        return previousPostNotes;
    }

    public void setPreviousPostNotes(String previousPostNotes) {
        this.previousPostNotes = previousPostNotes;
    }

    public String getPreviousVisitDate() {
        return previousVisitDate;
    }

    public void setPreviousVisitDate(String previousVisitDate) {
        this.previousVisitDate = previousVisitDate;
    }

    public List<AppointmentItemVo> getItems() {
        return items;
    }

    public void setItems(List<AppointmentItemVo> items) {
        this.items = items;
    }

    public Boolean getCanReview() {
        return canReview;
    }

    public void setCanReview(Boolean canReview) {
        this.canReview = canReview;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getRewardAvailable() {
        return rewardAvailable;
    }

    public void setRewardAvailable(Boolean rewardAvailable) {
        this.rewardAvailable = rewardAvailable;
    }

    public String getWaLink() {
        return waLink;
    }

    public void setWaLink(String waLink) {
        this.waLink = waLink;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
