package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Base64-encoded image payload.
 */
public class ImageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imageBase64;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
