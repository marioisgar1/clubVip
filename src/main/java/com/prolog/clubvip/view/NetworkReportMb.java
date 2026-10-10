package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessVo;
import com.prolog.clubvip.vo.CategoryCountVo;
import com.prolog.clubvip.vo.NetworkStatsVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Club VIP back office: network-wide report (KPIs, affiliates by category, affiliates with rating).
 */
@Named("networkReportMb")
@ViewScoped
public class NetworkReportMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type BUSINESS_LIST = new TypeToken<List<BusinessVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private NetworkStatsVo stats = new NetworkStatsVo();
    private List<BusinessVo> affiliates = new ArrayList<>();
    private List<BusinessVo> filtered;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            NetworkStatsVo s = RestClient.get("business/getNetworkStats", NetworkStatsVo.class, userId(), locale());
            stats = s != null ? s : new NetworkStatsVo();
        } catch (RestException e) {
            stats = new NetworkStatsVo();
            errors(e);
        }
        try {
            List<BusinessVo> list = RestClient.get("business/getAffiliates", BUSINESS_LIST, userId(), locale());
            affiliates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            affiliates = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    public List<CategoryCountVo> getByCategory() {
        return stats.getByCategory() != null ? stats.getByCategory() : new ArrayList<>();
    }

    public int categoryPercent(CategoryCountVo c) {
        int total = nz(stats.getAffiliates());
        long count = c != null && c.getCount() != null ? c.getCount() : 0L;
        return total > 0 ? (int) (count * 100 / total) : 0;
    }

    public int getVerifiedPercent() {
        int total = nz(stats.getAffiliates());
        return total > 0 ? nz(stats.getVerified()) * 100 / total : 0;
    }

    public int getAffiliatesCount() {
        return nz(stats.getAffiliates());
    }

    public int getVerifiedCount() {
        return nz(stats.getVerified());
    }

    public int getPendingCount() {
        return nz(stats.getPending());
    }

    public int getNewThisMonth() {
        return nz(stats.getNewThisMonth());
    }

    public int getAppointmentsLast30Days() {
        return nz(stats.getAppointmentsLast30Days());
    }

    public String stars(BigDecimal rating) {
        int full = rating != null ? rating.setScale(0, RoundingMode.HALF_UP).intValue() : 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= full ? '★' : '☆');
        }
        return sb.toString();
    }

    public String verificationBadge(String status) {
        if ("VERIFIED".equals(status)) {
            return "badge badge-success";
        }
        if ("REJECTED".equals(status)) {
            return "badge badge-danger";
        }
        return "badge badge-warning";
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

    // ------------------------------------------------------------------ helpers

    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public NetworkStatsVo getStats() {
        return stats;
    }

    public List<BusinessVo> getAffiliates() {
        return affiliates;
    }

    public List<BusinessVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<BusinessVo> filtered) {
        this.filtered = filtered;
    }
}
