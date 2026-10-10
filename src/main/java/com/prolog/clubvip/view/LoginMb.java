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
     * since the dashboard is appointment-centric).
     */
    static String homeFor(LoginResponseVo user) {
        Integer profile = user.getProfileId();
        // A consignment invitation waiting for the answer is the first thing the admin sees.
        if (profile != null && profile == SessionMb.PROFILE_ADMIN
                && user.getConsignmentPending() != null && user.getConsignmentPending() > 0) {
            return "/pages/consignment.xhtml";
        }
        if (profile != null && profile == SessionMb.PROFILE_OPERATOR) {
            return "/pages/affiliate.xhtml";
        }
        if (profile != null && profile == SessionMb.PROFILE_EMPLOYEE) {
            return "/pages/myAppointments.xhtml";
        }
        if (profile != null && profile == SessionMb.PROFILE_SELLER) {
            return "/pages/sale.xhtml";
        }
        if (profile != null && profile == SessionMb.PROFILE_ADMIN
                && Boolean.FALSE.equals(user.getModuleAppointments())) {
            return "/pages/sale.xhtml";
        }
        return "/pages/dashboard.xhtml";
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
}
