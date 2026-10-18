package com.prolog.clubvip.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.ServiceCommissionVo;
import com.prolog.clubvip.vo.ServiceVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Staff catalog (EMPLOYEE_VIEW): public profile, pay scheme, schedule, services, flags and app login.
 * employee/getEmployees · employee/saveUpdateEmployee · employee/deleteEmployees/{id}
 */
@Named("employeeMb")
@ViewScoped
public class EmployeeMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();
    private static final Type SERVICE_LIST = new TypeToken<List<ServiceVo>>() { }.getType();
    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    private SessionMb sessionMb;

    private List<EmployeeVo> employees = new ArrayList<>();
    private List<EmployeeVo> filtered;
    private List<ServiceVo> services = new ArrayList<>();
    /** One row per service of the business in the employee form: gives it? and its special commission. */
    private List<ServiceRow> serviceRows = new ArrayList<>();
    private EmployeeVo selected = new EmployeeVo();
    private Date newDateOff;
    private String newPhotoBase64;
    /** Second password field of the access section; only compared, never sent. */
    private String passwordConfirm;
    /** Goes into the avatar URLs so the browser fetches a photo again after a save. */
    private long photoVersion = System.currentTimeMillis();
    /** Hours of the business (starting point for an employee's own hours). */
    private List<com.prolog.clubvip.vo.DayHoursVo> businessWeek;
    /** Own hours of the employee being edited (used when ownHours is on). */
    private WeekModel week = WeekModel.of(null);

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<EmployeeVo> list = RestClient.get("employee/getEmployees", EMPLOYEE_LIST, userId(), sessionMb.getLocale());
            employees = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            employees = new ArrayList<>();
            errors(e.getMessages());
        }
        try {
            com.prolog.clubvip.vo.BusinessVo b = RestClient.get("business/getMyBusiness", com.prolog.clubvip.vo.BusinessVo.class,
                    userId(), sessionMb.getLocale());
            businessWeek = b != null ? b.getWeeklyHours() : null;
        } catch (RestException e) {
            businessWeek = null;
        }
        try {
            List<ServiceVo> list = RestClient.get("service/getServices", SERVICE_LIST, userId(), sessionMb.getLocale());
            services = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            services = new ArrayList<>();
        }
    }

    public void prepareNew() {
        selected = new EmployeeVo();
        selected.setPayScheme("COMMISSION");
        selected.setPayFrequency("WEEKLY");
        selected.setPayDay(null); // asked: the admin picks the weekday
        selected.setBookable(Boolean.TRUE);
        selected.setCanEditSchedule(Boolean.FALSE);
        selected.setDaysOff(new ArrayList<>());
        selected.setOwnHours(Boolean.FALSE);
        week = WeekModel.of(businessWeek);
        selected.setDateOffs(new ArrayList<>());
        selected.setServiceIds(new ArrayList<>());
        selected.setServiceCommissions(new ArrayList<>());
        buildServiceRows();
        newDateOff = null;
        newPhotoBase64 = null;
        passwordConfirm = null;
    }

    public void prepareEdit(EmployeeVo vo) {
        newDateOff = null;
        newPhotoBase64 = null;
        try {
            EmployeeVo full = RestClient.get("employee/getEmployee/" + vo.getId(), EmployeeVo.class,
                    userId(), sessionMb.getLocale());
            selected = full != null ? full : vo;
        } catch (RestException e) {
            selected = vo;
            errors(e.getMessages());
        }
        if (selected.getDaysOff() == null) {
            selected.setDaysOff(new ArrayList<>());
        }
        week = WeekModel.of(selected.getWeeklyHours() != null ? selected.getWeeklyHours() : businessWeek);
        if (selected.getDateOffs() == null) {
            selected.setDateOffs(new ArrayList<>());
        }
        if (selected.getServiceIds() == null) {
            selected.setServiceIds(new ArrayList<>());
        }
        buildServiceRows();
        selected.setPassword(null);
        passwordConfirm = null;
    }

    public void applyPhoto() {
        if (newPhotoBase64 != null && !newPhotoBase64.trim().isEmpty()) {
            selected.setPhotoBase64(ProductMb.stripDataUri(newPhotoBase64.trim()));
            newPhotoBase64 = null;
        }
    }

    public void removePhoto() {
        selected.setPhotoBase64(null);
        newPhotoBase64 = null;
    }

    public void addDateOff() {
        if (newDateOff == null) {
            return;
        }
        String day = newDateOff.toInstant().atZone(ZoneId.of(Constants.TIMEZONE)).toLocalDate().format(ISO_DAY);
        if (selected.getDateOffs() == null) {
            selected.setDateOffs(new ArrayList<>());
        }
        if (!selected.getDateOffs().contains(day)) {
            selected.getDateOffs().add(day);
            selected.getDateOffs().sort(null);
        }
        newDateOff = null;
    }

    public void removeDateOff(String day) {
        if (selected.getDateOffs() != null) {
            selected.getDateOffs().remove(day);
        }
    }

    public void save() {
        applyPhoto();
        if (isPayDayNeeded() && selected.getPayDay() == null) {
            warn(sessionMb.text("cat.employee.payDay.required"));
            return;
        }
        if (!isPayDayNeeded()) {
            selected.setPayDay(null);
        }
        // Hours: business hours, or own hours per weekday.
        boolean own = Boolean.TRUE.equals(selected.getOwnHours());
        selected.setOwnHours(own);
        selected.setWeeklyHours(own ? week.getDays() : null);
        // Blank / incomplete schedule = business schedule; blank login fields = no login change.
        if (isBlank(selected.getWorkStartTime()) || selected.getWorkStartTime().contains("_")) {
            selected.setWorkStartTime(null);
        }
        if (isBlank(selected.getWorkEndTime()) || selected.getWorkEndTime().contains("_")) {
            selected.setWorkEndTime(null);
        }
        if (isBlank(selected.getPassword())) {
            selected.setPassword(null);
        } else {
            // Same rules as sign-up and the new-password screen.
            if (selected.getPassword().length() < NewPasswordMb.MIN_LENGTH) {
                warn(sessionMb.text("newpwd.tooShort"));
                return;
            }
            if (!selected.getPassword().equals(passwordConfirm)) {
                warn(sessionMb.text("register.password.mismatch"));
                return;
            }
        }
        // Services the employee gives and the special commission of each one, both chosen here.
        List<Integer> serviceIds = new ArrayList<>();
        List<ServiceCommissionVo> commissions = new ArrayList<>();
        for (ServiceRow row : serviceRows) {
            if (row.isGives()) {
                serviceIds.add(row.getServiceId());
            }
            if (row.getSpecialPercent() != null) {
                commissions.add(new ServiceCommissionVo(row.getServiceId(), row.getSpecialPercent()));
            }
        }
        selected.setServiceIds(serviceIds);
        selected.setServiceCommissions(commissions);
        RestResponse r = RestClient.post("employee/saveUpdateEmployee", selected, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            // Own record (the owner is also staff): the topbar avatar shows the new photo right away.
            if (selected.getId() != null && selected.getId().equals(sessionMb.getUser().getEmployeeId())) {
                sessionMb.refreshUserPhoto(selected.getId());
                PrimeFaces.current().ajax().update("topbarForm");
            }
            passwordConfirm = null;
            photoVersion = System.currentTimeMillis();
            refresh();
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('employeeDialog').hide()");
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void delete(EmployeeVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("employee/deleteEmployees/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.deleted"));
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    // ------------------------------------------------------------------ services and their commission

    /** Rows of the "services" section from the catalog, the employee's services and special commissions. */
    private void buildServiceRows() {
        serviceRows = new ArrayList<>();
        for (ServiceVo s : services) {
            ServiceRow row = new ServiceRow(s.getId(), s.getName());
            row.setGives(selected.getServiceIds() != null && selected.getServiceIds().contains(s.getId()));
            if (selected.getServiceCommissions() != null) {
                for (ServiceCommissionVo c : selected.getServiceCommissions()) {
                    if (s.getId().equals(c.getServiceId())) {
                        row.setSpecialPercent(c.getCommissionPercent());
                    }
                }
            }
            serviceRows.add(row);
        }
    }

    /** Pencil: opens the special percent, starting from the employee's one. */
    public void editServiceCommission(ServiceRow row) {
        if (row.getSpecialPercent() == null) {
            row.setSpecialPercent(selected.getCommissionPercent() != null ? selected.getCommissionPercent() : BigDecimal.ZERO);
        }
        row.setEditing(true);
    }

    /** Check: closes the editor; the same percent as the employee's is no exception, so it is dropped. */
    public void doneServiceCommission(ServiceRow row) {
        if (row.getSpecialPercent() != null && selected.getCommissionPercent() != null
                && row.getSpecialPercent().compareTo(selected.getCommissionPercent()) == 0) {
            row.setSpecialPercent(null);
        }
        row.setEditing(false);
    }

    /** Back to the employee's percent for this service. */
    public void clearServiceCommission(ServiceRow row) {
        row.setSpecialPercent(null);
        row.setEditing(false);
    }

    /** "35 %" without trailing zeros. */
    public String pct(BigDecimal value) {
        return (value != null ? value.stripTrailingZeros().toPlainString() : "0") + " %";
    }

    public List<ServiceRow> getServiceRows() {
        return serviceRows;
    }

    /** A service in the employee form. */
    public static class ServiceRow implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Integer serviceId;
        private final String name;
        private boolean gives;
        private BigDecimal specialPercent;
        private boolean editing;

        public ServiceRow(Integer serviceId, String name) {
            this.serviceId = serviceId;
            this.name = name;
        }

        public Integer getServiceId() {
            return serviceId;
        }

        public String getName() {
            return name;
        }

        public boolean isGives() {
            return gives;
        }

        public void setGives(boolean gives) {
            this.gives = gives;
        }

        public BigDecimal getSpecialPercent() {
            return specialPercent;
        }

        public void setSpecialPercent(BigDecimal specialPercent) {
            this.specialPercent = specialPercent;
        }

        public boolean isEditing() {
            return editing;
        }

        public void setEditing(boolean editing) {
            this.editing = editing;
        }
    }

    // ------------------------------------------------------------------ display helpers

    /** "Comisión 35 %" / "Sueldo + 15 %" / "Sueldo fijo" according to the pay scheme. */
    public String schemeLabel(EmployeeVo vo) {
        if (vo == null || vo.getPayScheme() == null) {
            return "";
        }
        String pct = vo.getCommissionPercent() != null ? vo.getCommissionPercent().stripTrailingZeros().toPlainString() : "0";
        switch (vo.getPayScheme()) {
            case "COMMISSION":
                return sessionMb.text("cat.employee.scheme.COMMISSION") + " " + pct + " %";
            case "MIXED":
                return sessionMb.text("cat.employee.scheme.MIXED") + " + " + pct + " %";
            case "SALARY":
                return sessionMb.text("cat.employee.scheme.SALARY");
            default:
                return vo.getPayScheme();
        }
    }

    /** Weekday names of the days off, e.g. "Lunes, Domingo". */
    /** Own hours switched on: start from the business hours. */
    public void ownHoursChanged() {
        if (Boolean.TRUE.equals(selected.getOwnHours())) {
            week = WeekModel.of(selected.getWeeklyHours() != null ? selected.getWeeklyHours() : businessWeek);
        }
    }

    public WeekModel getWeek() {
        return week;
    }

    /** List column: own hours ("Lun–Vie 9:00–18:00 · Sáb 8:00–16:00 · Dom descansa") or business hours. */
    public String hoursLabel(EmployeeVo vo) {
        if (vo == null || !Boolean.TRUE.equals(vo.getOwnHours()) || vo.getWeeklyHours() == null) {
            return sessionMb.text("hours.employee.business");
        }
        String[] names = new String[7];
        for (int i = 0; i < 7; i++) {
            names[i] = sessionMb.text("weekday.short." + (i + 1));
        }
        return WeekModel.of(vo.getWeeklyHours()).describe(names, sessionMb.text("hours.employee.rest"));
    }

    public String daysOffLabel(EmployeeVo vo) {
        if (vo == null || vo.getDaysOff() == null || vo.getDaysOff().isEmpty()) {
            return "";
        }
        List<Integer> days = new ArrayList<>(vo.getDaysOff());
        days.sort(null);
        StringBuilder sb = new StringBuilder();
        for (Integer d : days) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(sessionMb.text("weekday." + d));
        }
        return sb.toString();
    }

    public String periodLabel(EmployeeVo vo) {
        if (vo == null || vo.getPeriodStart() == null) {
            return "";
        }
        return sessionMb.fmtDay(vo.getPeriodStart()) + " - " + sessionMb.fmtDay(vo.getPeriodEnd());
    }

    // ------------------------------------------------------------------ pay day

    /** Every frequency but DAILY asks which day the employee is paid. */
    public boolean isPayDayNeeded() {
        return selected != null && selected.getPayFrequency() != null && !"DAILY".equals(selected.getPayFrequency());
    }

    /** Frequency changed: the old pay day means something else now, so it is asked again. */
    public void frequencyChanged() {
        selected.setPayDay(null);
    }

    /** Weekdays, "15 y 30"-style pairs or days of the month, according to the frequency. */
    public List<SelectItem> getPayDayOptions() {
        List<SelectItem> items = new ArrayList<>();
        String f = selected != null ? selected.getPayFrequency() : null;
        if ("WEEKLY".equals(f)) {
            for (int d = 1; d <= 7; d++) {
                items.add(new SelectItem(d, sessionMb.text("weekday." + d)));
            }
        } else if ("BIWEEKLY".equals(f)) {
            for (int d = 1; d <= 15; d++) {
                items.add(new SelectItem(d, biweeklyLabel(d)));
            }
        } else if ("MONTHLY".equals(f)) {
            for (int d = 1; d <= 31; d++) {
                items.add(new SelectItem(d, monthlyLabel(d)));
            }
        }
        return items;
    }

    /** Note under the pay day: the commission cut closes the day before the pay day. */
    public String getPayDayNote() {
        if (!isPayDayNeeded() || selected.getPayDay() == null) {
            return sessionMb.text("cat.employee.payDay.note");
        }
        int d = selected.getPayDay();
        switch (selected.getPayFrequency()) {
            case "WEEKLY":
                return sessionMb.textf("cat.employee.payDay.note.WEEKLY", sessionMb.text("weekday." + d).toLowerCase(),
                        sessionMb.text("weekday." + (d == 1 ? 7 : d - 1)).toLowerCase());
            case "BIWEEKLY":
                return sessionMb.textf("cat.employee.payDay.note.BIWEEKLY", biweeklyLabel(d),
                        d == 1 ? sessionMb.text("cat.employee.payDay.cut.firstHalf")
                                : (d - 1) + " " + sessionMb.text("cat.employee.payDay.and") + " " + (d + 14));
            default:
                String cut = d == 1 ? sessionMb.text("cat.employee.payDay.cut.prevMonthEnd")
                        : d == 31 ? sessionMb.text("cat.employee.payDay.cut.monthEnd")
                        : sessionMb.textf("cat.employee.payDay.day", d - 1).toLowerCase();
                return sessionMb.textf("cat.employee.payDay.note.MONTHLY", monthlyLabel(d).toLowerCase(), cut);
        }
    }

    /** List column: "Viernes", "15 y 30", "Día 5"; blank when the employee has no pay day. */
    public String payDayLabel(EmployeeVo vo) {
        if (vo == null || vo.getPayDay() == null || vo.getPayFrequency() == null) {
            return "";
        }
        switch (vo.getPayFrequency()) {
            case "WEEKLY":
                return sessionMb.text("weekday." + vo.getPayDay());
            case "BIWEEKLY":
                return biweeklyLabel(vo.getPayDay());
            case "MONTHLY":
                return monthlyLabel(vo.getPayDay());
            default:
                return "";
        }
    }

    private String biweeklyLabel(int d) {
        String and = sessionMb.text("cat.employee.payDay.and");
        return d == 15 ? "15 " + and + " 30" : d + " " + and + " " + (d + 15);
    }

    private String monthlyLabel(int d) {
        return d == 31 ? sessionMb.text("cat.employee.payDay.lastDay") : sessionMb.textf("cat.employee.payDay.day", d);
    }

    public boolean isCommissionVisible() {
        return selected != null && ("COMMISSION".equals(selected.getPayScheme()) || "MIXED".equals(selected.getPayScheme()));
    }

    public boolean isSalaryVisible() {
        return selected != null && ("SALARY".equals(selected.getPayScheme()) || "MIXED".equals(selected.getPayScheme()));
    }

    public boolean hasLogin(EmployeeVo vo) {
        return vo != null && vo.getUserId() != null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void warn(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    /** Minimum password length shown in the access checklist (same rule as the new-password screen). */
    public int getMinLength() {
        return NewPasswordMb.MIN_LENGTH;
    }

    public long getPhotoVersion() {
        return photoVersion;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
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

    public List<EmployeeVo> getEmployees() {
        return employees;
    }

    public List<EmployeeVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<EmployeeVo> filtered) {
        this.filtered = filtered;
    }

    public List<ServiceVo> getServices() {
        return services;
    }

    public EmployeeVo getSelected() {
        return selected;
    }

    public void setSelected(EmployeeVo selected) {
        this.selected = selected;
    }

    public Date getNewDateOff() {
        return newDateOff;
    }

    public void setNewDateOff(Date newDateOff) {
        this.newDateOff = newDateOff;
    }

    public String getNewPhotoBase64() {
        return newPhotoBase64;
    }

    public void setNewPhotoBase64(String newPhotoBase64) {
        this.newPhotoBase64 = newPhotoBase64;
    }
}
