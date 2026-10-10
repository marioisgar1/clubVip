package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Customer statistics of a business.
 */
public class CustomerStatsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer total;

    private Integer newThisWeek;

    private Integer inactive;

    private Integer birthdaysToday;

    private Integer birthdaysMonth;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getNewThisWeek() {
        return newThisWeek;
    }

    public void setNewThisWeek(Integer newThisWeek) {
        this.newThisWeek = newThisWeek;
    }

    public Integer getInactive() {
        return inactive;
    }

    public void setInactive(Integer inactive) {
        this.inactive = inactive;
    }

    public Integer getBirthdaysToday() {
        return birthdaysToday;
    }

    public void setBirthdaysToday(Integer birthdaysToday) {
        this.birthdaysToday = birthdaysToday;
    }

    public Integer getBirthdaysMonth() {
        return birthdaysMonth;
    }

    public void setBirthdaysMonth(Integer birthdaysMonth) {
        this.birthdaysMonth = birthdaysMonth;
    }
}
