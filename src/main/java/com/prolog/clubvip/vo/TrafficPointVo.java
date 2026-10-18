package com.prolog.clubvip.vo;

import java.io.Serializable;

/** One point of a traffic series (a day, or an hour of the day). */
public class TrafficPointVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** yyyy-MM-dd for days, "0".."23" for hours. */
    private String label;
    private long visits;
    private long visitors;
    private long productViews;
    private long bookings;
    private long waClicks;

    public TrafficPointVo() {
    }

    public TrafficPointVo(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }

    public long getVisitors() {
        return visitors;
    }

    public void setVisitors(long visitors) {
        this.visitors = visitors;
    }

    public long getProductViews() {
        return productViews;
    }

    public void setProductViews(long productViews) {
        this.productViews = productViews;
    }

    public long getBookings() {
        return bookings;
    }

    public void setBookings(long bookings) {
        this.bookings = bookings;
    }

    public long getWaClicks() {
        return waClicks;
    }

    public void setWaClicks(long waClicks) {
        this.waClicks = waClicks;
    }
}
