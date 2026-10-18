package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessCategoryVo;
import com.prolog.clubvip.vo.BusinessVo;
import com.prolog.clubvip.vo.FileVo;
import com.prolog.clubvip.vo.ImageVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * "Mi negocio" (BUSINESS_VIEW): profile of the affiliate, verification / subscription status,
 * logo and license uploads. business/getMyBusiness?includeLogo=true · saveUpdateBusiness · uploadLogo · uploadLicense
 */
@Named("businessMb")
@ViewScoped
public class BusinessMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type CATEGORY_LIST = new TypeToken<List<BusinessCategoryVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private BusinessVo business = new BusinessVo();
    private List<BusinessCategoryVo> categories = new ArrayList<>();
    private List<Integer> closedWeekdays = new ArrayList<>();
    /** Hours per weekday being edited. */
    private WeekModel week = WeekModel.of(null);

    private String newLogoBase64;
    private String licenseBase64;
    private String licenseFileName;
    private String licenseContentType;

    @PostConstruct
    public void init() {
        loadCategories();
        refresh();
    }

    private void loadCategories() {
        try {
            List<BusinessCategoryVo> list = RestClient.get("category/getCategories", CATEGORY_LIST, userId(), sessionMb.getLocale());
            categories = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            categories = new ArrayList<>();
        }
    }

    public void refresh() {
        try {
            BusinessVo b = RestClient.get("business/getMyBusiness?includeLogo=true", BusinessVo.class, userId(), sessionMb.getLocale());
            business = b != null ? b : new BusinessVo();
        } catch (RestException e) {
            business = new BusinessVo();
            errors(e.getMessages());
        }
        business.setBankAccount(null);
        closedWeekdays = splitWeekdays(business.getClosedWeekdays());
        week = WeekModel.of(business.getWeeklyHours());
        newLogoBase64 = null;
        licenseBase64 = null;
        licenseFileName = null;
        licenseContentType = null;
    }

    public void save() {
        business.setClosedWeekdays(joinWeekdays());
        business.setWeeklyHours(week.getDays());
        cleanTimes();
        if (isBlank(business.getBankAccount())) {
            business.setBankAccount(null); // blank keeps the stored account
        }
        String logo = business.getLogoBase64();
        business.setLogoBase64(null); // never re-send the logo with the profile
        RestResponse r = RestClient.post("business/saveUpdateBusiness", business, userId(), sessionMb.getLocale());
        business.setLogoBase64(logo);
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    /** Normalises inputMask values: incomplete "09:__" or blank -> null. */
    private void cleanTimes() {
        business.setOpeningTime(normTime(business.getOpeningTime()));
        business.setClosingTime(normTime(business.getClosingTime()));
        business.setBreakStartTime(normTime(business.getBreakStartTime()));
        business.setBreakEndTime(normTime(business.getBreakEndTime()));
    }

    private static String normTime(String t) {
        return t == null || t.isBlank() || t.contains("_") ? null : t.trim();
    }

    /** Public booking link to share on social media (/b/{slug}/citas). */
    public String getCitasLink() {
        return business.getSlug() != null ? PublicLinks.link(business.getSlug(), PublicLinks.CITAS) : null;
    }

    /** Landing page link ("Mi página", /b/{slug}); until it is published it opens citas or tienda. */
    public String getPageLink() {
        return business.getSlug() != null ? PublicLinks.pageLink(business.getSlug()) : null;
    }

    /** Public online store link to share on social media (/b/{slug}/tienda). */
    public String getTiendaLink() {
        return business.getSlug() != null ? PublicLinks.link(business.getSlug(), PublicLinks.TIENDA) : null;
    }

    /** Consignment needs the sales module: switching sales off switches it off too. */
    public void onModulesChange() {
        if (business != null && !Boolean.TRUE.equals(business.getModuleSales())) {
            business.setModuleConsignment(Boolean.FALSE);
        }
    }

    public void uploadLogo() {
        if (isBlank(newLogoBase64)) {
            warn(sessionMb.text("cat.business.logo.none"));
            return;
        }
        ImageVo payload = new ImageVo();
        payload.setImageBase64(ProductMb.stripDataUri(newLogoBase64.trim()));
        RestResponse r = RestClient.post("business/uploadLogo", payload, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("cat.business.logo.saved"));
            sessionMb.refreshBusinessLogo();
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void uploadLicense() {
        if (isBlank(licenseBase64)) {
            warn(sessionMb.text("cat.business.license.none"));
            return;
        }
        FileVo payload = new FileVo();
        payload.setFileName(licenseFileName);
        payload.setContentType(isBlank(licenseContentType) ? "application/octet-stream" : licenseContentType);
        payload.setDataBase64(ProductMb.stripDataUri(licenseBase64.trim()));
        RestResponse r = RestClient.post("business/uploadLicense", payload, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("cat.business.license.saved"));
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    // ------------------------------------------------------------------ display helpers

    public boolean isVerified() {
        return "VERIFIED".equals(business.getVerificationStatus());
    }

    public boolean isRejected() {
        return "REJECTED".equals(business.getVerificationStatus());
    }

    public boolean isPendingVerification() {
        return business.getVerificationStatus() == null || "PENDING".equals(business.getVerificationStatus());
    }

    public String subscriptionBadge() {
        String s = business.getSubscriptionStatus();
        if (s == null) {
            return "badge badge-muted";
        }
        switch (s) {
            case "ACTIVE":
                return "badge badge-success";
            case "TRIAL":
                return "badge badge-info";
            case "SUSPENDED":
                return "badge badge-danger";
            default:
                return "badge badge-muted";
        }
    }

    private List<Integer> splitWeekdays(String csv) {
        List<Integer> days = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return days;
        }
        for (String s : csv.split(",")) {
            try {
                days.add(Integer.valueOf(s.trim()));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return days;
    }

    public WeekModel getWeek() {
        return week;
    }

    private String joinWeekdays() {
        if (closedWeekdays == null || closedWeekdays.isEmpty()) {
            return null;
        }
        List<Integer> sorted = new ArrayList<>(closedWeekdays);
        sorted.sort(null);
        StringBuilder sb = new StringBuilder();
        for (Integer d : sorted) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(d);
        }
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void warn(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    private void errors(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return;
        }
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, sessionMb.text("msg.error"), null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public BusinessVo getBusiness() {
        return business;
    }

    public void setBusiness(BusinessVo business) {
        this.business = business;
    }

    public List<BusinessCategoryVo> getCategories() {
        return categories;
    }

    public List<Integer> getClosedWeekdays() {
        return closedWeekdays;
    }

    public void setClosedWeekdays(List<Integer> closedWeekdays) {
        this.closedWeekdays = closedWeekdays;
    }

    public String getNewLogoBase64() {
        return newLogoBase64;
    }

    public void setNewLogoBase64(String newLogoBase64) {
        this.newLogoBase64 = newLogoBase64;
    }

    public String getLicenseBase64() {
        return licenseBase64;
    }

    public void setLicenseBase64(String licenseBase64) {
        this.licenseBase64 = licenseBase64;
    }

    public String getLicenseFileName() {
        return licenseFileName;
    }

    public void setLicenseFileName(String licenseFileName) {
        this.licenseFileName = licenseFileName;
    }

    public String getLicenseContentType() {
        return licenseContentType;
    }

    public void setLicenseContentType(String licenseContentType) {
        this.licenseContentType = licenseContentType;
    }
}
