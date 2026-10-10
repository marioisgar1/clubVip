package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Public business profile: summary plus services, staff, products and reviews.
 */
public class BusinessDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private BusinessSummaryVo business;

    private String description;

    private String closedWeekdays;

    private List<ServiceVo> services = new ArrayList<>();

    private List<EmployeeVo> employees = new ArrayList<>();

    private List<ProductVo> products = new ArrayList<>();

    private List<ReviewVo> reviews = new ArrayList<>();

    private Boolean onlinePaymentEnabled;

    public BusinessSummaryVo getBusiness() {
        return business;
    }

    public void setBusiness(BusinessSummaryVo business) {
        this.business = business;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClosedWeekdays() {
        return closedWeekdays;
    }

    public void setClosedWeekdays(String closedWeekdays) {
        this.closedWeekdays = closedWeekdays;
    }

    public List<ServiceVo> getServices() {
        return services;
    }

    public void setServices(List<ServiceVo> services) {
        this.services = services;
    }

    public List<EmployeeVo> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeVo> employees) {
        this.employees = employees;
    }

    public List<ProductVo> getProducts() {
        return products;
    }

    public void setProducts(List<ProductVo> products) {
        this.products = products;
    }

    public List<ReviewVo> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewVo> reviews) {
        this.reviews = reviews;
    }

    public Boolean getOnlinePaymentEnabled() {
        return onlinePaymentEnabled;
    }

    public void setOnlinePaymentEnabled(Boolean onlinePaymentEnabled) {
        this.onlinePaymentEnabled = onlinePaymentEnabled;
    }
}
