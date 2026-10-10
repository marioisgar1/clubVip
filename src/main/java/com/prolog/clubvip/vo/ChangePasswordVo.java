package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Password change request of the authenticated user.
 */
public class ChangePasswordVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String currentPassword;

    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
