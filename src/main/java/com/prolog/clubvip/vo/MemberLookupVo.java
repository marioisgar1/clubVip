package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Member found by its card at the counter: who it is and its points at the caller business. */
public class MemberLookupVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer customerId;

    private String memberNumber;

    private String fullName;

    private Integer points;

    private Boolean pointsEnabled;

    private BigDecimal pesosPerPoint;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Boolean getPointsEnabled() {
        return pointsEnabled;
    }

    public void setPointsEnabled(Boolean pointsEnabled) {
        this.pointsEnabled = pointsEnabled;
    }

    public BigDecimal getPesosPerPoint() {
        return pesosPerPoint;
    }

    public void setPesosPerPoint(BigDecimal pesosPerPoint) {
        this.pesosPerPoint = pesosPerPoint;
    }
}
