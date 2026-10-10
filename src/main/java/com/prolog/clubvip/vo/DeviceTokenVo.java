package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Push notification token registration (FCM).
 */
public class DeviceTokenVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String platform;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
