package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * "Create your new password" screen, shown right after signing in with the temporary password
 * e-mailed by "forgot password". The temporary password kept in the session user is sent as the
 * current one, so the user only types the new password twice.
 */
@Named("newPasswordMb")
@ViewScoped
public class NewPasswordMb implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int MIN_LENGTH = 8;

    @Inject
    private SessionMb sessionMb;

    private String newPassword;
    private String confirmPassword;

    public String save() {
        LoginResponseVo user = sessionMb.getUser();
        if (user == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        if (user.getTemporaryPassword() == null) {
            // The session was restored without it (e.g. server restart): sign in again with the e-mailed one.
            return error("newpwd.expired");
        }
        if (newPassword == null || newPassword.length() < MIN_LENGTH) {
            return error("newpwd.tooShort");
        }
        if (!newPassword.equals(confirmPassword)) {
            return error("newpwd.mismatch");
        }
        if (newPassword.equals(user.getTemporaryPassword())) {
            return error("newpwd.sameAsTemp");
        }
        Map<String, String> payload = new HashMap<>();
        payload.put("currentPassword", user.getTemporaryPassword());
        payload.put("newPassword", newPassword);
        RestResponse response = RestClient.post("security/changePassword", payload, user.getUserId(), sessionMb.getLocale());
        if (!response.isOk()) {
            List<String> errors = RestClient.extractErrors(response.getBody());
            // The temporary password expired while the screen was open: sign in again.
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    errors.isEmpty() ? sessionMb.text("newpwd.error") : errors.get(0), null));
            return null;
        }
        user.setMustChangePassword(false);
        user.setTemporaryPassword(null);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                sessionMb.text("newpwd.done"), ""));
        return LoginMb.homeFor(user) + "?faces-redirect=true";
    }

    private String error(String key) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, sessionMb.text(key), null));
        return null;
    }

    public int getMinLength() {
        return MIN_LENGTH;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
