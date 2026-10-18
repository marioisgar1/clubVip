package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Consignación: the receipt file of a payment.
 */
public class ConsignmentReceiptVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String contentType;
    /** Contenido en base64 (sin prefijo data:) */
    private String base64;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
