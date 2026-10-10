package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Daily report of appointments and revenue, broken down by employee, service and payment method.
 */
public class DailyReportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private Integer appointmentsCount;

    private Integer attended;

    private Integer pending;

    private Integer cancelled;

    private Integer noShow;

    private BigDecimal revenue;

    private BigDecimal servicesRevenue;

    private BigDecimal productsRevenue;

    private List<NameValueVo> byEmployee = new ArrayList<>();

    private List<NameValueVo> byService = new ArrayList<>();

    private List<NameValueVo> byPaymentMethod = new ArrayList<>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getAppointmentsCount() {
        return appointmentsCount;
    }

    public void setAppointmentsCount(Integer appointmentsCount) {
        this.appointmentsCount = appointmentsCount;
    }

    public Integer getAttended() {
        return attended;
    }

    public void setAttended(Integer attended) {
        this.attended = attended;
    }

    public Integer getPending() {
        return pending;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }

    public Integer getCancelled() {
        return cancelled;
    }

    public void setCancelled(Integer cancelled) {
        this.cancelled = cancelled;
    }

    public Integer getNoShow() {
        return noShow;
    }

    public void setNoShow(Integer noShow) {
        this.noShow = noShow;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getServicesRevenue() {
        return servicesRevenue;
    }

    public void setServicesRevenue(BigDecimal servicesRevenue) {
        this.servicesRevenue = servicesRevenue;
    }

    public BigDecimal getProductsRevenue() {
        return productsRevenue;
    }

    public void setProductsRevenue(BigDecimal productsRevenue) {
        this.productsRevenue = productsRevenue;
    }

    public List<NameValueVo> getByEmployee() {
        return byEmployee;
    }

    public void setByEmployee(List<NameValueVo> byEmployee) {
        this.byEmployee = byEmployee;
    }

    public List<NameValueVo> getByService() {
        return byService;
    }

    public void setByService(List<NameValueVo> byService) {
        this.byService = byService;
    }

    public List<NameValueVo> getByPaymentMethod() {
        return byPaymentMethod;
    }

    public void setByPaymentMethod(List<NameValueVo> byPaymentMethod) {
        this.byPaymentMethod = byPaymentMethod;
    }
}
