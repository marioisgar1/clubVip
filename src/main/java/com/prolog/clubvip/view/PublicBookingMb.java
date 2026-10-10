package com.prolog.clubvip.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentRequestVo;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.AvailabilityVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.ServiceVo;
import com.prolog.clubvip.vo.TimeSlotVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Public booking page (/b/{slug}/citas): the same steps as the app (service, who attends,
 * day, time) plus name and phone, without a Club VIP account. Posts appointment/bookGuest.
 */
@Named("publicBookingMb")
@ViewScoped
public class PublicBookingMb extends PublicBusinessMb {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private Integer employeeId;
    private LocalDate date;
    private AvailabilityVo availability;
    private String slotKey;
    private String customerName;
    private String customerPhone;
    private String notes;
    private AppointmentVo booked;

    /** Days with at least one free slot (yyyy-MM-dd) for the chosen service / professional. */
    private java.util.Set<String> availableDays = new java.util.HashSet<>();

    @PostConstruct
    public void init() {
        loadBusiness();
        date = null; // times only show once the customer taps a day (same as the app)
        // One service only: preselect it so the customer goes straight to "who" and "when".
        if (getServices().size() == 1) {
            selectService(getServices().get(0).getId());
        }
    }

    @Override
    public String getSection() {
        return PublicLinks.CITAS;
    }

    public List<ServiceVo> getServices() {
        List<ServiceVo> result = new ArrayList<>();
        if (detail != null && detail.getServices() != null) {
            for (ServiceVo s : detail.getServices()) {
                if (!Boolean.FALSE.equals(s.getActiveForBooking())) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    public ServiceVo getSelectedService() {
        for (ServiceVo s : getServices()) {
            if (s.getId().equals(serviceId)) {
                return s;
            }
        }
        return null;
    }

    /** Staff who perform the chosen service. */
    public List<EmployeeVo> getEmployees() {
        List<EmployeeVo> result = new ArrayList<>();
        ServiceVo s = getSelectedService();
        if (s == null || detail.getEmployees() == null) {
            return result;
        }
        for (EmployeeVo e : detail.getEmployees()) {
            if (s.getEmployeeIds() == null || s.getEmployeeIds().isEmpty() || s.getEmployeeIds().contains(e.getId())) {
                result.add(e);
            }
        }
        return result;
    }

    public void selectService(Integer id) {
        serviceId = id;
        employeeId = null;
        refreshDays();
    }

    /** null = anyone available. */
    public void selectEmployee(Integer id) {
        employeeId = id;
        refreshDays();
    }

    public void selectDate(String iso) {
        date = LocalDate.parse(iso);
        loadAvailability();
    }

    /** Month shown by the calendar. */
    private java.time.YearMonth calendarMonth;
    /** Months whose available days are already loaded. */
    private java.util.Set<java.time.YearMonth> loadedMonths = new java.util.HashSet<>();

    /** Service or professional changed: availability is recomputed; a chosen day is kept only if still free. */
    private void refreshDays() {
        availableDays = new java.util.HashSet<>();
        loadedMonths = new java.util.HashSet<>();
        if (calendarMonth == null) {
            calendarMonth = java.time.YearMonth.from(today());
        }
        loadMonth(calendarMonth);
        if (date != null && !availableDays.contains(date.toString())) {
            date = null;
        }
        loadAvailability();
    }

    private void loadMonth(java.time.YearMonth month) {
        if (serviceId == null || !isFound() || loadedMonths.contains(month)) {
            return;
        }
        java.lang.reflect.Type list = new com.google.gson.reflect.TypeToken<List<String>>() { }.getType();
        try {
            StringBuilder path = new StringBuilder("appointment/getAvailableDays?businessId=").append(getBusiness().getId())
                    .append("&serviceId=").append(serviceId).append("&month=").append(month);
            if (employeeId != null) {
                path.append("&employeeId=").append(employeeId);
            }
            List<String> days = RestClient.get(path.toString(), list, null, locale());
            if (days != null) {
                for (String d : days) {
                    availableDays.add(d.length() > 10 ? d.substring(0, 10) : d);
                }
            }
            loadedMonths.add(month);
        } catch (RestException e) {
            errors(e);
        }
    }

    public void previousMonth() {
        if (isCanPrevious()) {
            calendarMonth = calendarMonth.minusMonths(1);
            loadMonth(calendarMonth);
        }
    }

    public void nextMonth() {
        if (isCanNext()) {
            calendarMonth = calendarMonth.plusMonths(1);
            loadMonth(calendarMonth);
        }
    }

    public boolean isCanPrevious() {
        return calendarMonth != null && calendarMonth.isAfter(java.time.YearMonth.from(today()));
    }

    public boolean isCanNext() {
        return calendarMonth != null && calendarMonth.isBefore(java.time.YearMonth.from(getMaxDate()));
    }

    /** "Octubre 2026". */
    public String getMonthLabel() {
        if (calendarMonth == null) {
            return "";
        }
        String m = calendarMonth.getMonth().getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, locale());
        return Character.toUpperCase(m.charAt(0)) + m.substring(1) + " " + calendarMonth.getYear();
    }

    /** Monday-first initials: L M M J V S D. */
    public List<String> getWeekdays() {
        List<String> result = new ArrayList<>();
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) {
            result.add(d.getDisplayName(java.time.format.TextStyle.NARROW_STANDALONE, locale()).toUpperCase());
        }
        return result;
    }

    /** Month grid: leading blanks so day 1 falls on its weekday (Monday first), then every day. */
    public List<DayCell> getCalendarCells() {
        List<DayCell> cells = new ArrayList<>();
        if (calendarMonth == null) {
            return cells;
        }
        LocalDate first = calendarMonth.atDay(1);
        for (int i = 1; i < first.getDayOfWeek().getValue(); i++) {
            cells.add(new DayCell(null, false, false, false));
        }
        for (int d = 1; d <= calendarMonth.lengthOfMonth(); d++) {
            LocalDate day = calendarMonth.atDay(d);
            boolean inRange = !day.isBefore(today()) && !day.isAfter(getMaxDate());
            boolean available = inRange && availableDays.contains(day.toString());
            cells.add(new DayCell(day, available, day.equals(date), day.equals(today())));
        }
        return cells;
    }

    /** One cell of the month grid ({@code date} null = blank). */
    public static class DayCell implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final LocalDate date;
        private final boolean available;
        private final boolean selected;
        private final boolean today;

        DayCell(LocalDate date, boolean available, boolean selected, boolean today) {
            this.date = date;
            this.available = available;
            this.selected = selected;
            this.today = today;
        }

        public boolean isBlank() { return date == null; }
        public String getIso() { return date != null ? date.toString() : ""; }
        public int getDay() { return date != null ? date.getDayOfMonth() : 0; }
        public boolean isAvailable() { return available; }
        public boolean isSelected() { return selected; }
        public boolean isToday() { return today; }
    }

    public EmployeeVo getSelectedEmployee() {
        if (employeeId == null || detail == null || detail.getEmployees() == null) {
            return null;
        }
        for (EmployeeVo e : detail.getEmployees()) {
            if (employeeId.equals(e.getId())) {
                return e;
            }
        }
        return null;
    }

    /** Photo of a professional as a data URI (the public list already carries it), or null. */
    public String photo(EmployeeVo e) {
        return e != null && e.getPhotoBase64() != null && !e.getPhotoBase64().isBlank()
                ? "data:image/jpeg;base64," + e.getPhotoBase64() : null;
    }

    /** "sábado 4 de octubre". */
    public String getLongDate() {
        if (date == null) {
            return "";
        }
        java.util.Locale es = locale();
        String text = date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", es));
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public List<TimeSlotVo> getMorningSlots() {
        return slotsBetween(0, 12);
    }

    public List<TimeSlotVo> getAfternoonSlots() {
        return slotsBetween(12, 18);
    }

    public List<TimeSlotVo> getEveningSlots() {
        return slotsBetween(18, 24);
    }

    private List<TimeSlotVo> slotsBetween(int fromHour, int toHour) {
        List<TimeSlotVo> result = new ArrayList<>();
        for (TimeSlotVo s : getSlots()) {
            int h = Integer.parseInt(s.getStartTime().substring(0, 2));
            if (h >= fromHour && h < toHour) {
                result.add(s);
            }
        }
        return result;
    }

    public void loadAvailability() {
        availability = null;
        slotKey = null;
        if (serviceId == null || date == null || !isFound()) {
            return;
        }
        try {
            StringBuilder path = new StringBuilder("appointment/getAvailability?businessId=").append(getBusiness().getId())
                    .append("&serviceId=").append(serviceId).append("&date=").append(date);
            if (employeeId != null) {
                path.append("&employeeId=").append(employeeId);
            }
            availability = RestClient.get(path.toString(), AvailabilityVo.class, null, locale());
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Free times; with "anyone" each time shows once and the backend assigns whoever is free. */
    public List<TimeSlotVo> getSlots() {
        List<TimeSlotVo> all = availability != null && availability.getSlots() != null ? availability.getSlots() : new ArrayList<>();
        if (employeeId != null) {
            return all;
        }
        List<TimeSlotVo> unique = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (TimeSlotVo slot : all) {
            if (seen.add(slot.getStartTime())) {
                unique.add(slot);
            }
        }
        return unique;
    }

    public String slotKey(TimeSlotVo slot) {
        return slot.getStartTime() + "|" + (employeeId != null && slot.getEmployeeId() != null ? slot.getEmployeeId() : "");
    }

    public void selectSlot(String key) {
        slotKey = key;
    }

    public String getSelectedTime() {
        return slotKey != null ? slotKey.split("\\|", -1)[0] : null;
    }

    /** POST appointment/bookGuest. */
    public void book() {
        if (serviceId == null) {
            errorKey("op.msg.selectService");
            return;
        }
        if (slotKey == null) {
            errorKey("op.msg.selectSlot");
            return;
        }
        if (isBlank(customerName) || isBlank(customerPhone)) {
            errorKey("pub.book.contact.required");
            return;
        }
        String[] parts = slotKey.split("\\|", -1);
        AppointmentRequestVo req = new AppointmentRequestVo();
        req.setBusinessId(getBusiness().getId());
        req.setServiceId(serviceId);
        req.setEmployeeId(parts.length > 1 && !parts[1].isEmpty() ? Integer.valueOf(parts[1]) : employeeId);
        req.setDate(date.toString());
        req.setStartTime(parts[0]);
        req.setCustomerName(customerName.trim());
        req.setCustomerPhone(customerPhone.trim());
        req.setPreNotes(isBlank(notes) ? null : notes.trim());
        try {
            booked = RestClient.post("appointment/bookGuest", req, AppointmentVo.class, null, locale());
        } catch (RestException e) {
            errors(e);
            loadAvailability(); // the slot may have just been taken: refresh the hours
        }
    }

    /** Book another appointment after a successful one. */
    public void again() {
        booked = null;
        serviceId = null;
        employeeId = null;
        availability = null;
        slotKey = null;
        notes = null;
        if (getServices().size() == 1) {
            selectService(getServices().get(0).getId());
        }
    }

    /** WhatsApp to the business with the appointment summary. */
    public String getWhatsAppConfirm() {
        if (booked == null || getBusiness().getWaLink() == null) {
            return null;
        }
        String text = sessionMb.text("pub.book.wa").replace("{business}", getBusiness().getName())
                .replace("{service}", String.valueOf(booked.getServiceName()))
                .replace("{date}", sessionMb.fmtDay(booked.getDate()))
                .replace("{time}", String.valueOf(booked.getStartTime()))
                .replace("{name}", customerName != null ? customerName.trim() : "");
        return WhatsAppLink.withText(getBusiness().getWaLink(), text);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public LocalDate getMinDate() {
        return today();
    }

    public LocalDate getMaxDate() {
        return today().plusDays(30);
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentVo getBooked() {
        return booked;
    }
}
