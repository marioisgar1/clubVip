package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Login request: {@code username} carries the e-mail or the phone (there is no separate user
 * name) and the raw password.
 */
public class LoginRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
