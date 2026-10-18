package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** A share of the visits: by section, source, device or referrer. */
public class TrafficSliceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private long visits;
    private BigDecimal percent;

    public TrafficSliceVo() {
    }

    public TrafficSliceVo(String key, long visits, BigDecimal percent) {
        this.key = key;
        this.visits = visits;
        this.percent = percent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }
}
