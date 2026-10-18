package com.prolog.clubvip.view;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessMembershipVo;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.UrlVo;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * "Activa tu membresía": the two plans (monthly / annual) paid on Stripe Checkout, shown right after
 * the business signs up and whenever its membership is not active. Coming back from Stripe with
 * {@code ?session_id=} activates the membership at once (the webhook confirms it later as well).
 */
@Named("membershipMb")
@ViewScoped
public class MembershipMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionMb sessionMb;

    private BusinessMembershipVo status = new BusinessMembershipVo();
    private String sessionId;
    private String canceled;
    private boolean justPaid;

    /** f:viewAction: syncs the payment when Stripe sent the customer back, otherwise reads the status. */
    public void init() {
        if (sessionMb.getUser() == null) {
            return;
        }
        if (sessionId != null && !sessionId.isBlank()) {
            Map<String, String> body = new HashMap<>();
            body.put("sessionId", sessionId);
            try {
                BusinessMembershipVo vo = RestClient.post("membership/sync", body, BusinessMembershipVo.class,
                        sessionMb.getUserId(), sessionMb.getLocale());
                if (vo != null) {
                    status = vo;
                    justPaid = !vo.isRequired();
                }
            } catch (RestException e) {
                addErrors(e.getMessages());
                load();
            }
        } else {
            load();
            if (canceled != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, sessionMb.text("mem.canceled"), null));
            }
        }
        syncSession();
    }

    private void load() {
        try {
            BusinessMembershipVo vo = RestClient.get("membership/status", BusinessMembershipVo.class,
                    sessionMb.getUserId(), sessionMb.getLocale());
            if (vo != null) {
                status = vo;
            }
        } catch (RestException e) {
            addErrors(e.getMessages());
        }
    }

    /** The session copy drives the filter: once paid, the rest of the portal opens without logging in again. */
    private void syncSession() {
        LoginResponseVo user = sessionMb.getUser();
        if (user != null && status != null && status.getStatus() != null) {
            user.setMembershipRequired(status.isRequired());
            user.setMembershipPastDue(status.isPastDue());
            user.setMembershipGraceUntil(status.getGraceUntil());
            user.setMembershipStatus(status.getStatus());
        }
    }

    /** Sends the owner to Stripe Checkout for the chosen plan (MONTHLY | ANNUAL). */
    public void checkout(String plan) {
        Map<String, String> body = new HashMap<>();
        body.put("plan", plan);
        body.put("returnBase", returnBase());
        if (!redirectTo("membership/checkout", body)) {
            // Refused because Stripe already has this business's payment: show it as paid instead of the plans.
            load();
            syncSession();
            justPaid = !status.isRequired();
        }
    }

    /** Stripe's billing portal: change card, invoices, cancel. */
    public void portal() {
        Map<String, String> body = new HashMap<>();
        body.put("returnBase", returnBase());
        redirectTo("membership/portal", body);
    }

    /** This portal's own address (scheme, host, port, context path), so Stripe comes back here: local or production. */
    private String returnBase() {
        jakarta.servlet.http.HttpServletRequest req = (jakarta.servlet.http.HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = req.getRequestURL().toString();
        int ctx = url.indexOf(req.getContextPath(), url.indexOf("://") + 3);
        return ctx > 0 ? url.substring(0, ctx + req.getContextPath().length()) : url;
    }

    /** True when the browser was sent to Stripe. */
    private boolean redirectTo(String path, Map<String, String> body) {
        try {
            UrlVo vo = RestClient.post(path, body, UrlVo.class, sessionMb.getUserId(), sessionMb.getLocale());
            if (vo != null && vo.getUrl() != null) {
                FacesContext.getCurrentInstance().getExternalContext().redirect(vo.getUrl());
                FacesContext.getCurrentInstance().responseComplete();
                return true;
            }
            addErrors(List.of(sessionMb.text("mem.error")));
        } catch (RestException e) {
            addErrors(e.getMessages());
        } catch (IOException e) {
            addErrors(List.of(sessionMb.text("mem.error")));
        }
        return false;
    }

    public String goHome() {
        LoginResponseVo user = sessionMb.getUser();
        if (user == null) {
            return "/login.xhtml?faces-redirect=true";
        }
        return LoginMb.homeFor(user) + "?faces-redirect=true";
    }

    private void addErrors(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, sessionMb.text("mem.error"), null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------ view helpers

    public boolean isAdmin() {
        return sessionMb.isAdmin();
    }

    /** Suspended while a chargeback is under review: no plans to pick, only the notice. */
    public boolean isDisputeOpen() {
        return status != null && "SUSPENDED".equals(status.getStatus()) && "OPEN".equals(status.getDisputeStatus());
    }

    public boolean isActive() {
        return status != null && !status.isRequired() && !status.isPastDue();
    }

    public String getMonthly() {
        return money(status.getMonthlyPrice());
    }

    public String getAnnual() {
        return money(status.getAnnualPrice());
    }

    public String getSaving() {
        return money(status.getAnnualSaving());
    }

    /** Annual price spread over 12 months ("equivale a $X al mes"). */
    public String getAnnualPerMonth() {
        return status.getAnnualPrice() != null
                ? money(status.getAnnualPrice().divide(BigDecimal.valueOf(12), 0, java.math.RoundingMode.HALF_UP)) : "";
    }

    private String money(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("es", "MX"));
        nf.setMinimumFractionDigits(amount.stripTrailingZeros().scale() > 0 ? 2 : 0);
        nf.setMaximumFractionDigits(2);
        return "$" + nf.format(amount) + (status.getCurrency() != null ? " " + status.getCurrency() : "");
    }

    public String getUntilText() {
        return day(status.getUntil());
    }

    public String getGraceText() {
        return day(status.getGraceUntil());
    }

    /** yyyy-MM-dd -> "12 de octubre de 2026" in the session language. */
    private String day(String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(iso);
            String pattern = "en".equals(sessionMb.getLanguage()) ? "MMMM d, yyyy" : "d 'de' MMMM 'de' yyyy";
            return d.format(java.time.format.DateTimeFormatter.ofPattern(pattern, sessionMb.getLocale()));
        } catch (RuntimeException e) {
            return iso;
        }
    }

    public BusinessMembershipVo getStatus() {
        return status;
    }

    public boolean isJustPaid() {
        return justPaid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCanceled() {
        return canceled;
    }

    public void setCanceled(String canceled) {
        this.canceled = canceled;
    }
}
