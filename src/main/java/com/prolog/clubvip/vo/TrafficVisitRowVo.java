package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.Date;

/** One visit in the "latest visits" list of the traffic report. */
public class TrafficVisitRowVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date startedAt;
    private String source;
    private String section;
    private String device;
    private String referrer;
    private int durationSeconds;
    private int productViews;
    private int waClicks;
    private int bookings;
    private boolean newVisitor;
    /** Name of the logged-in customer (apps), when known. */
    private String customerName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getProductViews() {
        return productViews;
    }

    public void setProductViews(int productViews) {
        this.productViews = productViews;
    }

    public int getWaClicks() {
        return waClicks;
    }

    public void setWaClicks(int waClicks) {
        this.waClicks = waClicks;
    }

    public int getBookings() {
        return bookings;
    }

    public void setBookings(int bookings) {
        this.bookings = bookings;
    }

    public boolean isNewVisitor() {
        return newVisitor;
    }

    public void setNewVisitor(boolean newVisitor) {
        this.newVisitor = newVisitor;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
