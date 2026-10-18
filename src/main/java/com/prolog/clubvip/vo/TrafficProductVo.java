package com.prolog.clubvip.vo;

import java.io.Serializable;

/** A product of the "most viewed" ranking of the traffic report. */
public class TrafficProductVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;
    private String name;
    private String barcode;
    private String referenceCode;
    private String size;
    private String color;
    /** Times the product was opened. */
    private long views;
    /** Different visits that opened it. */
    private long visits;

    public TrafficProductVo() {
    }

    public TrafficProductVo(Integer productId, String name, String barcode, String referenceCode,
                            String size, String color, long views, long visits) {
        this.productId = productId;
        this.name = name;
        this.barcode = barcode;
        this.referenceCode = referenceCode;
        this.size = size;
        this.color = color;
        this.views = views;
        this.visits = visits;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }
}
