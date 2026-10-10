package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessVo;
import com.prolog.clubvip.vo.SubscriptionVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Club VIP back office: subscription status of every affiliate.
 */
@Named("subscriptionMb")
@ViewScoped
public class SubscriptionMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type BUSINESS_LIST = new TypeToken<List<BusinessVo>>() { }.getType();
    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    private SessionMb sessionMb;

    private List<BusinessVo> affiliates = new ArrayList<>();
    private List<BusinessVo> filtered;
    private BusinessVo selected = new BusinessVo();
    private SubscriptionVo subscription = new SubscriptionVo();
    private LocalDate until;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<BusinessVo> list = RestClient.get("business/getAffiliates", BUSINESS_LIST, userId(), locale());
            affiliates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            affiliates = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    public void prepareEdit(BusinessVo b) {
        selected = b;
        subscription = new SubscriptionVo();
        subscription.setBusinessId(b.getId());
        subscription.setSubscriptionStatus(b.getSubscriptionStatus() != null ? b.getSubscriptionStatus() : "TRIAL");
        until = null;
        if (b.getSubscriptionUntil() != null && !b.getSubscriptionUntil().isBlank()) {
            try {
                until = LocalDate.parse(b.getSubscriptionUntil(), ISO_DAY);
            } catch (Exception ignored) {
                until = null;
            }
        }
    }

    public void save() {
        subscription.setSubscriptionUntil(until != null ? until.format(ISO_DAY) : null);
        RestResponse r = RestClient.post("business/updateSubscription", subscription, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('subscriptionDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public String subscriptionBadge(String status) {
        if ("ACTIVE".equals(status)) {
            return "badge badge-success";
        }
        if ("TRIAL".equals(status)) {
            return "badge badge-info";
        }
        if ("SUSPENDED".equals(status) || "CANCELLED".equals(status)) {
            return "badge badge-danger";
        }
        return "badge badge-muted";
    }

    /** True when the subscription end date is in the past. */
    public boolean isExpired(BusinessVo b) {
        if (b == null || b.getSubscriptionUntil() == null || b.getSubscriptionUntil().isBlank()) {
            return false;
        }
        try {
            return LocalDate.parse(b.getSubscriptionUntil(), ISO_DAY).isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ helpers

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            error(m);
        }
    }

    private void errors(RestResponse r) {
        for (String m : RestClient.extractErrors(r.getBody())) {
            error(m);
        }
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // ------------------------------------------------------------------ getters / setters

    public List<BusinessVo> getAffiliates() {
        return affiliates;
    }

    public List<BusinessVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<BusinessVo> filtered) {
        this.filtered = filtered;
    }

    public BusinessVo getSelected() {
        return selected;
    }

    public SubscriptionVo getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionVo subscription) {
        this.subscription = subscription;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }
}
