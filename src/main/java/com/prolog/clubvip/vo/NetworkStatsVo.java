package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Network-wide statistics for the platform administrator.
 */
public class NetworkStatsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer affiliates;

    private Integer verified;

    private Integer pending;

    private Integer newThisMonth;

    private Integer appointmentsLast30Days;

    private List<CategoryCountVo> byCategory = new ArrayList<>();

    public Integer getAffiliates() {
        return affiliates;
    }

    public void setAffiliates(Integer affiliates) {
        this.affiliates = affiliates;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public Integer getPending() {
        return pending;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }

    public Integer getNewThisMonth() {
        return newThisMonth;
    }

    public void setNewThisMonth(Integer newThisMonth) {
        this.newThisMonth = newThisMonth;
    }

    public Integer getAppointmentsLast30Days() {
        return appointmentsLast30Days;
    }

    public void setAppointmentsLast30Days(Integer appointmentsLast30Days) {
        this.appointmentsLast30Days = appointmentsLast30Days;
    }

    public List<CategoryCountVo> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<CategoryCountVo> byCategory) {
        this.byCategory = byCategory;
    }
}
