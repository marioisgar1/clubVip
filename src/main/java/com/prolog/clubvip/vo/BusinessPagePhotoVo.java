package com.prolog.clubvip.vo;

import java.io.Serializable;

/** Photo of "Mi página". Upload: kind (COVER | GALLERY) + imageBase64; the answer carries the id. */
public class BusinessPagePhotoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String kind;

    private Integer sortOrder;

    private String imageBase64;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
