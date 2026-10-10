package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Standard backend error element: HTTP 4xx/5xx bodies are List&lt;GenericErrorVo&gt;.
 */
public class GenericErrorVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
