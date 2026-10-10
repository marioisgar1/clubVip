package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking request from the app or from the business counter.
 */
public class AppointmentRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer businessId;

    private Integer serviceId;

    private Integer employeeId;

    private String date;

    private String startTime;

    private String preNotes;

    private String paymentMode;

    private List<AppointmentItemVo> items = new ArrayList<>();

    private Integer customerId;

    private String customerPhone;

    private String customerName;

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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

    public String getPreNotes() {
        return preNotes;
    }

    public void setPreNotes(String preNotes) {
        this.preNotes = preNotes;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<AppointmentItemVo> getItems() {
        return items;
    }

    public void setItems(List<AppointmentItemVo> items) {
        this.items = items;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
