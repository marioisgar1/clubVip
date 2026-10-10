package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessVo;
import com.prolog.clubvip.vo.FileVo;
import com.prolog.clubvip.vo.NetworkStatsVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Club VIP back office: affiliate verification (license review, approve / reject) and network KPIs.
 */
@Named("affiliateMb")
@ViewScoped
public class AffiliateMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type BUSINESS_LIST = new TypeToken<List<BusinessVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private NetworkStatsVo stats = new NetworkStatsVo();
    private String statusFilter = "PENDING"; // PENDING | VERIFIED | REJECTED | ALL
    private List<BusinessVo> affiliates = new ArrayList<>();
    private List<BusinessVo> filtered;
    private BusinessVo selected = new BusinessVo();
    private String rejectReason;
    private FileVo license;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        loadStats();
        loadAffiliates();
    }

    public void loadStats() {
        try {
            NetworkStatsVo s = RestClient.get("business/getNetworkStats", NetworkStatsVo.class, userId(), locale());
            stats = s != null ? s : new NetworkStatsVo();
        } catch (RestException e) {
            stats = new NetworkStatsVo();
            errors(e);
        }
    }

    public void loadAffiliates() {
        String path = "business/getAffiliates";
        if (statusFilter != null && !"ALL".equals(statusFilter)) {
            path += "?verificationStatus=" + RestClient.encode(statusFilter);
        }
        try {
            List<BusinessVo> list = RestClient.get(path, BUSINESS_LIST, userId(), locale());
            affiliates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            affiliates = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    // ------------------------------------------------------------------ actions

    public void approve(BusinessVo b) {
        RestResponse r = RestClient.post("business/approveVerification/" + b.getId(), null, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.affiliate.approved"));
            refresh();
        } else {
            errors(r);
        }
    }

    public void prepareReject(BusinessVo b) {
        selected = b;
        rejectReason = null;
    }

    public void reject() {
        if (selected == null || selected.getId() == null) {
            return;
        }
        if (rejectReason == null || rejectReason.isBlank()) {
            error(sessionMb.text("rel.affiliate.reasonRequired"));
            return;
        }
        RestResponse r = RestClient.post("business/rejectVerification/" + selected.getId()
                + "?reason=" + RestClient.encode(rejectReason), null, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.affiliate.rejected"));
            PrimeFaces.current().executeScript("PF('rejectDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public void viewLicense(BusinessVo b) {
        selected = b;
        license = null;
        try {
            license = RestClient.get("business/getLicense/" + b.getId(), FileVo.class, userId(), locale());
            if (license == null || license.getDataBase64() == null) {
                error(sessionMb.text("rel.affiliate.noDocument"));
                return;
            }
            PrimeFaces.current().executeScript("PF('licenseDialog').show()");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ presentation helpers

    public boolean isLicenseLoaded() {
        return license != null && license.getDataBase64() != null;
    }

    public boolean isLicensePdf() {
        return isLicenseLoaded() && license.getContentType() != null
                && license.getContentType().toLowerCase().contains("pdf");
    }

    public String getLicenseDataUrl() {
        if (!isLicenseLoaded()) {
            return "";
        }
        String type = license.getContentType() != null ? license.getContentType() : "application/octet-stream";
        return "data:" + type + ";base64," + license.getDataBase64();
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

    public NetworkStatsVo getStats() {
        return stats;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
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

    public BusinessVo getSelected() {
        return selected;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public FileVo getLicense() {
        return license;
    }
}
