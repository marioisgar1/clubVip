package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Give points: memberNumber plus a saleId (points from the sale total) or a purchase amount. */
public class PointsAwardVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String memberNumber;

    private Integer saleId;

    private BigDecimal amount;

    private String note;

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
