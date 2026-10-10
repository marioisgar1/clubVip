package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Base64-encoded file payload with its name and content type.
 */
public class FileVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileName;

    private String contentType;

    private String dataBase64;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDataBase64() {
        return dataBase64;
    }

    public void setDataBase64(String dataBase64) {
        this.dataBase64 = dataBase64;
    }
}
