package com.prolog.clubvip.vo;

import java.io.Serializable;

/** An event inside a visit (traffic/event): PRODUCT_VIEW (productId), WA_CLICK or BOOKING (detail). */
public class TrafficEventVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer visitId;
    private String type;
    private Integer productId;
    private String detail;

    public TrafficEventVo() {
    }

    public TrafficEventVo(Integer visitId, String type, Integer productId, String detail) {
        this.visitId = visitId;
        this.type = type;
        this.productId = productId;
        this.detail = detail;
    }

    public Integer getVisitId() {
        return visitId;
    }

    public void setVisitId(Integer visitId) {
        this.visitId = visitId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
