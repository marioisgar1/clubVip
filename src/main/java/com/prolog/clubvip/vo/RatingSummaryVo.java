package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Rating summary of a business: average, count and star distribution.
 */
public class RatingSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal average;

    private Integer count;

    private Integer fiveStars;

    private Integer fourStars;

    private Integer threeStars;

    private Integer twoStars;

    private Integer oneStar;

    private Integer pendingReplies;

    public BigDecimal getAverage() {
        return average;
    }

    public void setAverage(BigDecimal average) {
        this.average = average;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getFiveStars() {
        return fiveStars;
    }

    public void setFiveStars(Integer fiveStars) {
        this.fiveStars = fiveStars;
    }

    public Integer getFourStars() {
        return fourStars;
    }

    public void setFourStars(Integer fourStars) {
        this.fourStars = fourStars;
    }

    public Integer getThreeStars() {
        return threeStars;
    }

    public void setThreeStars(Integer threeStars) {
        this.threeStars = threeStars;
    }

    public Integer getTwoStars() {
        return twoStars;
    }

    public void setTwoStars(Integer twoStars) {
        this.twoStars = twoStars;
    }

    public Integer getOneStar() {
        return oneStar;
    }

    public void setOneStar(Integer oneStar) {
        this.oneStar = oneStar;
    }

    public Integer getPendingReplies() {
        return pendingReplies;
    }

    public void setPendingReplies(Integer pendingReplies) {
        this.pendingReplies = pendingReplies;
    }
}
