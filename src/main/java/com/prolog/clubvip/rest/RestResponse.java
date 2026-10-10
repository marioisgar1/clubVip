package com.prolog.clubvip.rest;

/**
 * Lightweight wrapper for a raw REST call result (status + body text).
 */
public class RestResponse {

    private final int status;
    private final String body;

    public RestResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public boolean isOk() {
        return status >= 200 && status < 300;
    }
}
