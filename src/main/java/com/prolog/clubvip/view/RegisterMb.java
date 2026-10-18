package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessCategoryVo;
import com.prolog.clubvip.vo.BusinessRegistrationVo;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.PaymentMethodVo;
import com.prolog.clubvip.vo.SaleLocationVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Public self-registration of an affiliate business (POST security/registerBusiness).
 * On success the owner is logged in and sent to the dashboard.
 */
@Named("registerMb")
@ViewScoped
public class RegisterMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionMb sessionMb;

    private BusinessRegistrationVo registration = newRegistration();

    private static BusinessRegistrationVo newRegistration() {
        BusinessRegistrationVo r = new BusinessRegistrationVo();
        r.setOperationMode("FIXED");
        // Everything off: the owner switches on what she needs (the server asks for at least citas or ventas).
        r.setModuleAppointments(Boolean.FALSE);
        r.setModuleSales(Boolean.FALSE);
        r.setModuleConsignment(Boolean.FALSE);
        r.setModulePayroll(Boolean.FALSE);
        r.setPointsEnabled(Boolean.FALSE);
        return r;
    }

    // ---- Step 1: what the business will use. The sales step only shows when sales is on. ----

    public void onModulesChange() {
        if (!isSalesOn()) {
            registration.setModuleConsignment(Boolean.FALSE);
        }
    }

    public boolean isSalesOn() {
        return !Boolean.FALSE.equals(registration.getModuleSales());
    }

    public boolean isAppointmentsOn() {
        return !Boolean.FALSE.equals(registration.getModuleAppointments());
    }

    public int getTotalSteps() {
        return isSalesOn() ? 6 : 5;
    }

    /** Number shown on a section head: modules, business, contact, sales, files, account. */
    public int step(String section) {
        switch (section) {
            case "modules": return 1;
            case "business": return 2;
            case "contact": return 3;
            case "sales": return 4;
            case "files": return isSalesOn() ? 5 : 4;
            case "account": return isSalesOn() ? 6 : 5;
            default: return 0;
        }
    }
    private List<BusinessCategoryVo> categories = new ArrayList<>();
    private List<Integer> closedWeekdays = new ArrayList<>();
    /** Hours per weekday (default Monday to Saturday 09:00-19:00). */
    private WeekModel week = WeekModel.standard("09:00", "19:00");
    private String passwordConfirm;
    private boolean privacyAccepted;

    /** Typical sale locations offered at sign-up (type code = {@code sale_location.location_type}). */
    private static final String[] LOCATION_TYPES = { "TIANGUIS", "BAZAR", "MALL", "MARKETPLACE", "SHOP" };

    /** Payment methods offered at sign-up; cash is checked by default. */
    private final List<PaymentOption> paymentOptions = new ArrayList<>();
    /** Where the business sells: the typical ones as checkboxes plus the ones the user adds (at least one checked). */
    private final List<LocationOption> locationOptions = new ArrayList<>();

    @PostConstruct
    public void init() {
        registration.setOpeningTime("09:00");
        registration.setClosingTime("18:00");
        loadCategories();
        paymentOptions.add(new PaymentOption(sessionMb.text("register.pay.CASH"), false, true));
        paymentOptions.add(new PaymentOption(sessionMb.text("register.pay.CREDIT_CARD"), false, false));
        paymentOptions.add(new PaymentOption(sessionMb.text("register.pay.DEBIT_CARD"), false, false));
        paymentOptions.add(new PaymentOption(sessionMb.text("register.pay.TRANSFER"), false, false));
        paymentOptions.add(new PaymentOption(null, true, false));
        for (String type : LOCATION_TYPES) {
            locationOptions.add(new LocationOption(sessionMb.text("register.loc." + type), type, false));
        }
        onModeChange();
    }

    /** Checks the typical location for the mode (own premises, bazaar, online) while none is checked yet. */
    public void onModeChange() {
        for (LocationOption o : locationOptions) {
            if (o.isSelected()) {
                return;
            }
        }
        String mode = registration.getOperationMode() != null ? registration.getOperationMode() : "FIXED";
        String type = "ONLINE".equals(mode) ? "MARKETPLACE" : "MOBILE".equals(mode) ? "BAZAR" : "SHOP";
        for (LocationOption o : locationOptions) {
            if (type.equals(o.getType())) {
                o.setSelected(true);
            }
        }
    }

    /** Adds an "other" location with a free name (checked, removable). */
    public void addLocation() {
        locationOptions.add(new LocationOption(null, "OTHER", true));
    }

    public void removeLocation(int index) {
        if (index >= 0 && index < locationOptions.size() && locationOptions.get(index).isCustom()) {
            locationOptions.remove(index);
        }
    }

    private void loadCategories() {
        try {
            Type t = new TypeToken<List<BusinessCategoryVo>>() { }.getType();
            List<BusinessCategoryVo> list = RestClient.get("category/getCategories", t, null, sessionMb.getLocale());
            categories = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            categories = new ArrayList<>();
            addError(e.getMessages());
        }
    }

    /** Whether the selected category requires a professional license to be verified. */
    /** Physical premises: address and closed days make sense. */
    public boolean isFixedLocation() {
        return registration.getOperationMode() == null || "FIXED".equals(registration.getOperationMode());
    }

    /** 100% online business: no address, no city. */
    public boolean isOnline() {
        return "ONLINE".equals(registration.getOperationMode());
    }

    public boolean isLicenseRequired() {
        if (registration.getCategoryId() == null) {
            return false;
        }
        for (BusinessCategoryVo c : categories) {
            if (registration.getCategoryId().equals(c.getId())) {
                return Boolean.TRUE.equals(c.getRequiresLicense());
            }
        }
        return false;
    }

    public void onCategoryChange() {
        if (!isLicenseRequired()) {
            registration.setLicenseBase64(null);
            registration.setLicenseFileName(null);
            registration.setLicenseContentType(null);
        }
    }

    public String doRegister() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (!privacyAccepted) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("register.privacy.required"), null));
            return null;
        }
        if (registration.getPassword() == null || registration.getPassword().length() < NewPasswordMb.MIN_LENGTH) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("newpwd.tooShort"), null));
            return null;
        }
        if (registration.getPassword() == null || !registration.getPassword().equals(passwordConfirm)) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("register.password.mismatch"), null));
            return null;
        }
        if (isLicenseRequired() && isBlank(registration.getLicenseBase64())) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("register.license.required"), null));
            return null;
        }
        registration.setPrivacyAccepted(Boolean.TRUE);
        registration.setClosedWeekdays(joinWeekdays());
        if (isBlank(registration.getContactPhone())) {
            registration.setContactPhone(registration.getGroupPhone());
        }
        // The e-mail is always the administrator's login.
        if (!isFixedLocation()) {
            registration.setAddress(null);
            closedWeekdays = new ArrayList<>();
        }
        if (isOnline()) {
            registration.setCity(null);
        }
        registration.setClosedWeekdays(joinWeekdays());
        registration.setWeeklyHours(week.getDays());
        if (!fillPaymentsAndLocations(fc)) {
            return null;
        }
        try {
            LoginResponseVo user = RestClient.post("security/registerBusiness", registration,
                    LoginResponseVo.class, null, sessionMb.getLocale());
            if (user == null || user.getUserId() == null) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", null));
                return null;
            }
            LoginMb.storeSession(sessionMb, user);
            // The codes are already on their way: confirm e-mail and phones before the first screen.
            return LoginMb.homeFor(user) + "?faces-redirect=true";
        } catch (RestException e) {
            addError(e.getMessages());
            return null;
        }
    }

    /** Copies the checked payment methods and the named locations into the request; false when one is missing. */
    private boolean fillPaymentsAndLocations(FacesContext fc) {
        if (!isSalesOn()) {
            // No point of sale: the server creates the default cash method and main location.
            registration.setPaymentMethods(null);
            registration.setSaleLocations(null);
            return true;
        }
        List<PaymentMethodVo> methods = new ArrayList<>();
        for (PaymentOption o : paymentOptions) {
            if (!o.isSelected()) {
                continue;
            }
            if (isBlank(o.getName())) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        sessionMb.text("register.pay.ownCredit.required"), null));
                return false;
            }
            PaymentMethodVo m = new PaymentMethodVo();
            m.setName(o.getName().trim());
            m.setCommissionPercent(o.getCommission() != null ? o.getCommission() : BigDecimal.ZERO);
            m.setCredit(o.isOwnCredit());
            m.setStatus(1);
            methods.add(m);
        }
        if (methods.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("register.payments.required"), null));
            return false;
        }
        List<SaleLocationVo> locations = new ArrayList<>();
        for (LocationOption o : locationOptions) {
            if (o.isSelected() && !isBlank(o.getName())) {
                SaleLocationVo l = new SaleLocationVo();
                l.setName(o.getName().trim());
                l.setLocationType(o.getType());
                l.setStatus(1);
                locations.add(l);
            }
        }
        if (locations.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    sessionMb.text("register.locations.required"), null));
            return false;
        }
        registration.setPaymentMethods(methods);
        registration.setSaleLocations(locations);
        return true;
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

    private void addError(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return;
        }
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // ------------------------------------------------------------------ getters / setters

    public BusinessRegistrationVo getRegistration() {
        return registration;
    }

    public void setRegistration(BusinessRegistrationVo registration) {
        this.registration = registration;
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

    /** Minimum password length shown in the sign-up checklist (same rule as the new-password screen). */
    public int getMinLength() {
        return NewPasswordMb.MIN_LENGTH;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public boolean isPrivacyAccepted() {
        return privacyAccepted;
    }

    public void setPrivacyAccepted(boolean privacyAccepted) {
        this.privacyAccepted = privacyAccepted;
    }

    public List<PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    public List<LocationOption> getLocationOptions() {
        return locationOptions;
    }

    /** One sale location row: a typical one (fixed name and type) or one the user added (free name, type OTHER). */
    public static class LocationOption implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private final String type;
        private final boolean custom;
        private boolean selected;

        public LocationOption(String name, String type, boolean custom) {
            this.name = name;
            this.type = type;
            this.custom = custom;
            this.selected = custom;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public boolean isCustom() {
            return custom;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    /** One payment method row of the sign-up form; the own-credit row has an editable name. */
    public static class PaymentOption implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private final boolean ownCredit;
        private boolean selected;
        private BigDecimal commission;

        public PaymentOption(String name, boolean ownCredit, boolean selected) {
            this.name = name;
            this.ownCredit = ownCredit;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOwnCredit() {
            return ownCredit;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public BigDecimal getCommission() {
            return commission;
        }

        public void setCommission(BigDecimal commission) {
            this.commission = commission;
        }
    }
}
