package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.security.BusinessLogo;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.MenuItemVo;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

/**
 * Login screen backing bean. Authenticates against the backend, stores the session user and
 * lands each profile on its home page. Customers (profile 4) only use the mobile app.
 */
@Named("loginMb")
@ViewScoped
public class LoginMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();

    @Inject
    private SessionMb sessionMb;

    private String username;
    private String password;

    /** "Forgot password" panel: shown instead of the login form; {@code forgotSent} = the "check your e-mail" step. */
    private boolean forgotMode;
    private boolean forgotSent;
    private String forgotUsername;

    public void showForgot() {
        forgotMode = true;
        forgotSent = false;
        forgotUsername = username;
    }

    public void backToLogin() {
        if (forgotSent && forgotUsername != null && !forgotUsername.isBlank()) {
            username = forgotUsername.trim();
        }
        forgotMode = false;
        forgotSent = false;
        password = null;
    }

    /** Asks the backend to e-mail a temporary password; it answers OK even for unknown accounts. */
    public void sendForgot() {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", forgotUsername != null ? forgotUsername.trim() : null);
        RestResponse response = RestClient.post("security/forgotPassword", payload, null, sessionMb.getLocale());
        if (response.isOk()) {
            forgotSent = true;
            return;
        }
        List<String> errors = RestClient.extractErrors(response.getBody());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                errors.isEmpty() ? sessionMb.text("forgot.error") : errors.get(0), null));
    }

    public String doLogin() {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);

        RestResponse response = RestClient.post("security/login", payload, null, sessionMb.getLocale());
        if (response.isOk()) {
            LoginResponseVo user = GSON.fromJson(response.getBody(), LoginResponseVo.class);
            if (user == null || user.getProfileId() == null
                    || user.getProfileId() == SessionMb.PROFILE_CUSTOMER) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, sessionMb.text("login.customer.notallowed"), null));
                return null;
            }
            if (user.isMustChangePassword()) {
                user.setTemporaryPassword(password);
            }
            storeSession(sessionMb, user);
            return homeFor(user) + "?faces-redirect=true";
        } else {
            String message = "Error";
            List<String> errors = RestClient.extractErrors(response.getBody());
            if (!errors.isEmpty()) {
                message = errors.get(0);
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
            return null;
        }
    }

    /** Puts the user in the HTTP session and the session bean (also used after self-registration). */
    static void storeSession(SessionMb sessionMb, LoginResponseVo user) {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(true);
        session.setAttribute(Constants.SESSION_USER, user);
        BusinessLogo.invalidate(session); // never keep the logo of a previous login
        sessionMb.setUser(user);
    }

    /**
     * Landing page per profile: operator -> affiliates, employee -> own agenda, seller -> point of
     * sale, admin -> dashboard (or the point of sale when the business has no appointments module,
     * since the dashboard is appointment-centric). The preferred page is only used when the user's
     * menu allows it (a module switched off in the business removes its pages); otherwise the first
     * page of the menu is used, so nobody lands on "access denied" right after logging in.
     */
    static String homeFor(LoginResponseVo user) {
        if (user.isMustChangePassword()) {
            return "/pages/newPassword.xhtml";
        }
        if (user.isMembershipRequired()) {
            return "/pages/membership.xhtml";
        }
        if (user.isVerificationRequired()) {
            return "/pages/verifyContact.xhtml";
        }
        Integer profile = user.getProfileId();
        // A consignment invitation waiting for the answer is the first thing the admin sees.
        if (profile != null && profile == SessionMb.PROFILE_ADMIN
                && user.getConsignmentPending() != null && user.getConsignmentPending() > 0) {
            return allowedOrFirst(user, "/pages/consignment.xhtml");
        }
        if (profile != null && profile == SessionMb.PROFILE_OPERATOR) {
            return allowedOrFirst(user, "/pages/affiliate.xhtml");
        }
        if (profile != null && profile == SessionMb.PROFILE_EMPLOYEE) {
            return allowedOrFirst(user, Boolean.FALSE.equals(user.getModuleAppointments())
                    ? "/pages/sale.xhtml" : "/pages/myAppointments.xhtml");
        }
        if (profile != null && profile == SessionMb.PROFILE_SELLER) {
            return allowedOrFirst(user, "/pages/sale.xhtml");
        }
        if (profile != null && profile == SessionMb.PROFILE_ADMIN
                && Boolean.FALSE.equals(user.getModuleAppointments())) {
            return allowedOrFirst(user, "/pages/sale.xhtml");
        }
        return "/pages/dashboard.xhtml";
    }

    /**
     * The preferred page when the menu contains it; otherwise the first menu page (lowest
     * sortOrder) and, with no menu page at all, the dashboard (always allowed by the filter).
     */
    private static String allowedOrFirst(LoginResponseVo user, String preferred) {
        List<MenuItemVo> menu = user.getMenu();
        if (menu == null || menu.isEmpty()) {
            return "/pages/dashboard.xhtml";
        }
        MenuItemVo first = null;
        for (MenuItemVo item : menu) {
            if (item.getOutcomeWeb() == null) {
                continue;
            }
            if (preferred.equals(item.getOutcomeWeb())) {
                return preferred;
            }
            if (first == null || (item.getSortOrder() != null && first.getSortOrder() != null
                    && item.getSortOrder() < first.getSortOrder())) {
                first = item;
            }
        }
        return first != null ? first.getOutcomeWeb() : "/pages/dashboard.xhtml";
    }

    /** Club VIP support number for the wa.me link on the login page. */
    public String getWhatsappNumber() {
        return Constants.CLUBVIP_WHATSAPP;
    }

    /** Ready-to-use wa.me link to Club VIP support with a localized, URL-encoded greeting. */
    public String getWhatsappLink() {
        return WhatsAppLink.withText(Constants.CLUBVIP_WHATSAPP, sessionMb.text("login.whatsapp.text"));
    }

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

    public boolean isForgotMode() {
        return forgotMode;
    }

    public boolean isForgotSent() {
        return forgotSent;
    }

    public String getForgotUsername() {
        return forgotUsername;
    }

    public void setForgotUsername(String forgotUsername) {
        this.forgotUsername = forgotUsername;
    }
}
