package com.prolog.clubvip.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /** Chosen services in the order the customer picked them (done back to back, same professional). */
    private List<Integer> serviceIds = new ArrayList<>();
    /** The customer pressed "Seguir": professional, day and time are shown for the chosen services. */
    private boolean proceeded;
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
        preselectSingleService();
    }

    /** One service only: preselect it and go straight to "who" and "when". */
    private void preselectSingleService() {
        if (getServices().size() == 1) {
            serviceIds.add(getServices().get(0).getId());
            proceed();
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

    /**
     * Bookable services grouped by specialty (alphabetical; services without one go last, in a group
     * with a null name that the page labels "Otros servicios"). Order inside a group is the catalog order.
     */
    public List<ServiceGroup> getServiceGroups() {
        Map<String, ServiceGroup> byKey = new LinkedHashMap<>();
        ServiceGroup other = null;
        for (ServiceVo s : getServices()) {
            String name = s.getSpecialty() != null && !s.getSpecialty().isBlank() ? s.getSpecialty().trim() : null;
            ServiceGroup g;
            if (name == null) {
                if (other == null) {
                    other = new ServiceGroup("other", null);
                }
                g = other;
            } else {
                String key = groupKey(name);
                g = byKey.computeIfAbsent(key, k -> new ServiceGroup(k, name));
            }
            g.add(s, serviceIds.contains(s.getId()));
        }
        List<ServiceGroup> result = new ArrayList<>(byKey.values());
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        if (other != null) {
            result.add(other);
        }
        return result;
    }

    /** More than one specialty: the page shows collapsible groups and specialty chips. */
    public boolean isGrouped() {
        return getServiceGroups().size() > 1;
    }

    /** Filter bar (search + sort, plus chips when grouped) only when there is something to filter. */
    public boolean isShowServiceFilters() {
        return isGrouped() || getServices().size() >= 6;
    }

    /** "Pestañas y cejas" -> "pestanas-y-cejas": stable id for the group, its chip and the JS state. */
    private static String groupKey(String name) {
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String key = plain.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return key.isEmpty() ? "g" + Math.abs(name.hashCode()) : key;
    }

    /** One specialty of the service list. */
    public static class ServiceGroup implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String key;
        private final String name;
        private final List<ServiceVo> services = new ArrayList<>();
        private BigDecimal minPrice;
        private boolean selected;

        ServiceGroup(String key, String name) {
            this.key = key;
            this.name = name;
        }

        void add(ServiceVo s, boolean isSelected) {
            services.add(s);
            if (s.getPrice() != null && (minPrice == null || s.getPrice().compareTo(minPrice) < 0)) {
                minPrice = s.getPrice();
            }
            selected |= isSelected;
        }

        public String getKey() { return key; }
        public String getName() { return name; }
        public List<ServiceVo> getServices() { return services; }
        public int getCount() { return services.size(); }
        public BigDecimal getMinPrice() { return minPrice; }
        /** The chosen service is in this group (it stays open after a choice; the others fold). */
        public boolean isSelected() { return selected; }
    }

    /** First chosen service (null when none). */
    public ServiceVo getSelectedService() {
        List<ServiceVo> chosen = getSelectedServices();
        return chosen.isEmpty() ? null : chosen.get(0);
    }

    /** Chosen services in the order they were picked. */
    public List<ServiceVo> getSelectedServices() {
        List<ServiceVo> result = new ArrayList<>();
        List<ServiceVo> all = getServices();
        for (Integer id : serviceIds) {
            for (ServiceVo s : all) {
                if (s.getId().equals(id)) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    public boolean isSelected(Integer id) {
        return serviceIds.contains(id);
    }

    public int getSelectedCount() {
        return serviceIds.size();
    }

    /** Sum of the durations of the chosen services (the length of the visit). */
    public int getTotalMinutes() {
        int total = 0;
        for (ServiceVo s : getSelectedServices()) {
            total += s.getDurationMinutes() != null && s.getDurationMinutes() > 0 ? s.getDurationMinutes() : 30;
        }
        return total;
    }

    /** "1 h 30 min" / "45 min". */
    public String getTotalDurationLabel() {
        int m = getTotalMinutes();
        if (m < 60) {
            return m + " min";
        }
        return (m / 60) + " h" + (m % 60 > 0 ? " " + (m % 60) + " min" : "");
    }

    public java.math.BigDecimal getTotalPrice() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (ServiceVo s : getSelectedServices()) {
            if (s.getPrice() != null) {
                total = total.add(s.getPrice());
            }
        }
        return total;
    }

    /** "Gel Semipermanente + Pestañas clásicas". */
    public String getSelectedNames() {
        StringBuilder sb = new StringBuilder();
        for (ServiceVo s : getSelectedServices()) {
            if (sb.length() > 0) {
                sb.append(" + ");
            }
            sb.append(s.getName());
        }
        return sb.toString();
    }

    /** End of the visit for the chosen time ("11:30"), or null. */
    public String getSelectedEndTime() {
        String t = getSelectedTime();
        if (t == null) {
            return null;
        }
        java.time.LocalTime end = java.time.LocalTime.parse(t.length() == 5 ? t : t.substring(0, 5)).plusMinutes(getTotalMinutes());
        return end.toString();
    }

    public boolean isProceeded() {
        return proceeded;
    }

    /** "serviceIds=3,7" for the availability calls. */
    private String serviceParam() {
        StringBuilder sb = new StringBuilder("&serviceIds=");
        for (int i = 0; i < serviceIds.size(); i++) {
            sb.append(i > 0 ? "," : "").append(serviceIds.get(i));
        }
        return sb.toString();
    }

    /** Staff who perform every chosen service (one person does the whole visit). */
    public List<EmployeeVo> getEmployees() {
        List<EmployeeVo> result = new ArrayList<>();
        List<ServiceVo> chosen = getSelectedServices();
        if (chosen.isEmpty() || detail.getEmployees() == null) {
            return result;
        }
        for (EmployeeVo e : detail.getEmployees()) {
            boolean doesAll = true;
            for (ServiceVo s : chosen) {
                if (s.getEmployeeIds() != null && !s.getEmployeeIds().isEmpty() && !s.getEmployeeIds().contains(e.getId())) {
                    doesAll = false;
                    break;
                }
            }
            if (doesAll) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Adds or removes a service. Changing the list hides the next steps until "Seguir" again, since
     * the professionals and free times depend on all the services together.
     */
    public void selectService(Integer id) {
        if (id == null) {
            return;
        }
        if (serviceIds.contains(id)) {
            serviceIds.remove(id);
        } else {
            serviceIds.add(id);
        }
        proceeded = false;
        employeeId = null;
        date = null;
        availability = null;
        slotKey = null;
    }

    /** "Seguir": show who attends, the days and the times for the whole visit. */
    public void proceed() {
        if (serviceIds.isEmpty()) {
            errorKey("op.msg.selectService");
            return;
        }
        proceeded = true;
        if (employeeId != null && getEmployees().stream().noneMatch(e -> employeeId.equals(e.getId()))) {
            employeeId = null;
        }
        refreshDays();
    }

    /** "Editar" on the summary: back to the service list keeping the choice (who / day / time start over). */
    public void editServices() {
        proceeded = false;
        date = null;
        availability = null;
        slotKey = null;
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
        if (serviceIds.isEmpty() || !proceeded || !isFound() || loadedMonths.contains(month)) {
            return;
        }
        java.lang.reflect.Type list = new com.google.gson.reflect.TypeToken<List<String>>() { }.getType();
        try {
            StringBuilder path = new StringBuilder("appointment/getAvailableDays?businessId=").append(getBusiness().getId())
                    .append(serviceParam()).append("&month=").append(month);
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
        if (serviceIds.isEmpty() || !proceeded || date == null || !isFound()) {
            return;
        }
        try {
            StringBuilder path = new StringBuilder("appointment/getAvailability?businessId=").append(getBusiness().getId())
                    .append(serviceParam()).append("&date=").append(date);
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
        if (serviceIds.isEmpty()) {
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
        req.setServiceId(serviceIds.get(0));
        req.setServiceIds(new ArrayList<>(serviceIds));
        req.setEmployeeId(parts.length > 1 && !parts[1].isEmpty() ? Integer.valueOf(parts[1]) : employeeId);
        req.setDate(date.toString());
        req.setStartTime(parts[0]);
        req.setCustomerName(customerName.trim());
        req.setCustomerPhone(customerPhone.trim());
        req.setPreNotes(isBlank(notes) ? null : notes.trim());
        try {
            booked = RestClient.post("appointment/bookGuest", req, AppointmentVo.class, null, locale());
            bookedServices = getSelectedServices();
            bookedTotalLabel = getTotalDurationLabel();
            bookedTotalPrice = getTotalPrice();
            bookedEndTime = getSelectedEndTime();
            track("BOOKING", null, booked != null && booked.getId() != null ? String.valueOf(booked.getId()) : null);
        } catch (RestException e) {
            errors(e);
            loadAvailability(); // the slot may have just been taken: refresh the hours
        }
    }

    /** Book another appointment after a successful one. */
    public void again() {
        booked = null;
        serviceIds = new ArrayList<>();
        proceeded = false;
        employeeId = null;
        date = null;
        availability = null;
        slotKey = null;
        notes = null;
        bookedServices = new ArrayList<>();
        preselectSingleService();
    }

    /**
     * WhatsApp to the business with the appointment summary, formatted the way the store message is
     * (bold, bullets, emojis). Text only: a click-to-chat link cannot carry an image.
     */
    public String getWhatsAppConfirm() {
        if (booked == null || getBusiness().getWaLink() == null) {
            return null;
        }
        String name = customerName != null ? customerName.trim() : "";
        StringBuilder t = new StringBuilder(sessionMb.text("pub.book.wa.intro")
                .replace("{business}", "*" + getBusiness().getName() + "*")
                .replace("{name}", "*" + name + "*"));
        t.append("\n\n");
        if (bookedServices.size() > 1) {
            for (ServiceVo s : bookedServices) {
                t.append("• *").append(s.getName()).append("*\n");
            }
        } else {
            t.append("✂️ *").append(booked.getServiceName()).append("*\n");
        }
        // Who attends: the business checks that person's availability before confirming.
        String employee = booked.getEmployeeName();
        t.append("👤 ").append(sessionMb.text("pub.book.wa.with")).append(' ')
                .append(isBlank(employee) ? sessionMb.text("pub.book.wa.anyEmployee") : employee.trim()).append('\n');
        t.append("📅 ").append(getBookedLongDate()).append('\n');
        t.append("🕒 ").append(booked.getStartTime());
        if (!isBlank(bookedEndTime)) {
            t.append(" – ").append(bookedEndTime);
        }
        t.append(" hrs");
        if (!isBlank(bookedTotalLabel)) {
            t.append(" · ").append(bookedTotalLabel);
        }
        if (bookedTotalPrice != null) {
            t.append("\n💰 ").append(sessionMb.fmtMoney(bookedTotalPrice));
        }
        t.append("\n\n").append(sessionMb.text("pub.book.wa.question"));
        return WhatsAppLink.withText(getBusiness().getWaLink(), t.toString());
    }

    /** "Jueves 22 de octubre" of the booked day (from the appointment, not the picker: "again" resets it). */
    private String getBookedLongDate() {
        try {
            String pattern = "es".equals(locale().getLanguage()) ? "EEEE d 'de' MMMM" : "EEEE, MMMM d";
            String text = LocalDate.parse(booked.getDate()).format(java.time.format.DateTimeFormatter.ofPattern(pattern, locale()));
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);
        } catch (RuntimeException e) {
            return sessionMb.fmtDay(booked.getDate());
        }
    }

    // ------------------------------------------------------------------ link preview (og:*)

    public String getShareTitle() {
        return getBusiness().getName() + " · " + sessionMb.text("pub.book.kicker");
    }

    public String getShareDescription() {
        if (detail != null && !isBlank(detail.getDescription())) {
            return detail.getDescription();
        }
        return isBlank(getBusiness().getSpecialty()) ? sessionMb.text("pub.book.share.desc") : getBusiness().getSpecialty();
    }

    public String getShareUrl() {
        return PublicLinks.link(slug, PublicLinks.CITAS);
    }

    /** The logo shown in this page's hero (what the business recognizes in WhatsApp), else the cover. */
    @Override
    public String getShareImage() {
        return isHasLogo() ? PublicLinks.absolute("/publicImage?logo=" + getBusiness().getId()) : super.getShareImage();
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

    /** First chosen service (null = none): the page only asks "is there a choice". */
    public Integer getServiceId() {
        return serviceIds.isEmpty() ? null : serviceIds.get(0);
    }

    /** What was booked (kept for the "done" card, the selection is reset on "again"). */
    private List<ServiceVo> bookedServices = new ArrayList<>();
    private String bookedTotalLabel;
    private java.math.BigDecimal bookedTotalPrice;
    private String bookedEndTime;

    public List<ServiceVo> getBookedServices() {
        return bookedServices;
    }

    public String getBookedNames() {
        StringBuilder sb = new StringBuilder();
        for (ServiceVo s : bookedServices) {
            sb.append(sb.length() > 0 ? " + " : "").append(s.getName());
        }
        return sb.toString();
    }

    public String getBookedTotalLabel() {
        return bookedTotalLabel;
    }

    public java.math.BigDecimal getBookedTotalPrice() {
        return bookedTotalPrice;
    }

    public String getBookedEndTime() {
        return bookedEndTime;
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
