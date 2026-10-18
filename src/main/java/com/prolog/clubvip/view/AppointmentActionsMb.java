package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentItemVo;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.CloseAppointmentVo;
import com.prolog.clubvip.vo.CloseResultVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.MessageComposeVo;
import com.prolog.clubvip.vo.PreNotesVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.ServiceVo;

import org.primefaces.PrimeFaces;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Detail + lifecycle actions of one visit (start, close with charge, cancel, no-show, reassign,
 * WhatsApp reminder). A visit is every service a customer booked together ({@code visitId});
 * the actions apply to all of them at once and the charge is a single one, where services and
 * products can be added at the counter. Backs /WEB-INF/includes/appointmentDialogs.xhtml, which
 * the schedule, day and "my appointments" pages include; the owning page is passed to each action
 * so its list is refreshed afterwards.
 */
@Named("appointmentActionsMb")
@ViewScoped
public class AppointmentActionsMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();
    private static final Type SERVICE_LIST = new TypeToken<List<ServiceVo>>() { }.getType();
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();

    private AppointmentVo selected;
    private VisitRow visit = VisitRow.of(null);
    private String cancelReason;
    private Integer reassignEmployeeId;
    private List<EmployeeVo> employees;

    // close form
    private CloseAppointmentVo closeForm = new CloseAppointmentVo();
    private List<CloseLine> closeServices = new ArrayList<>();
    /** Tip of the close dialog: "0" (none), "10" / "15" / "20" (percent) or "OTHER" (tipCustomValue in tipCustomMode). */
    private String tipChoice = "0";
    private BigDecimal tipCustomValue;
    private String tipCustomMode = "PERCENT";
    private String tipPaymentMethod = "CASH";
    private List<AppointmentItemVo> closeItems = new ArrayList<>();
    private CloseResultVo result;
    private List<ServiceVo> serviceOptions;
    private List<ProductVo> productOptions;
    private Integer addServiceId;
    private String addProductId;

    // when the visit happened (start / close dialogs): NOW or the BOOKED time, both editable
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private String timeMode = "NOW";
    private LocalDate timeDate;
    private String timeStart;
    private String timeEnd;
    /** What NOW proposed when the dialog opened: sent untouched, the backend takes its own "now". */
    private String proposedNow;
    private boolean closing;
    /**
     * Page that opened the start / close dialog, refreshed after the action. Kept here because the
     * {@code owner} param does not reach the buttons placed after the nested visitTime include.
     */
    private Refreshable dialogOwner;

    /** One service of the charge: an appointment of the visit, or a service added at the counter. */
    public static class CloseLine implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer appointmentId;
        private Integer serviceId;
        private String name;
        private String employeeName;
        private Integer durationMinutes;
        private BigDecimal price;
        private boolean done = true;
        private boolean extra;

        public Integer getAppointmentId() { return appointmentId; }
        public Integer getServiceId() { return serviceId; }
        public String getName() { return name; }
        public String getEmployeeName() { return employeeName; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public BigDecimal getPrice() { return price; }
        public boolean isDone() { return done; }
        public void setDone(boolean done) { this.done = done; }
        public boolean isExtra() { return extra; }
    }

    // ------------------------------------------------------------------ detail

    /** Load the full appointment (expediente, products, previous visit, siblings of the visit) and keep it as selected. */
    public void open(AppointmentVo row) {
        selected = null;
        visit = VisitRow.of(null);
        cancelReason = null;
        reassignEmployeeId = null;
        if (row == null || row.getId() == null) {
            return;
        }
        try {
            selected = RestClient.get("appointment/getAppointment/" + row.getId(), AppointmentVo.class,
                    userId(), locale());
            if (selected == null) {
                selected = row;
            }
        } catch (RestException e) {
            selected = row;
            errors(e);
        }
        visit = VisitRow.of(selected);
    }

    public void openById(Integer id) {
        AppointmentVo row = new AppointmentVo();
        row.setId(id);
        open(row);
    }

    private void reload() {
        if (selected != null) {
            open(selected);
        }
    }

    private void refreshOwner(Refreshable owner) {
        refreshOwner(owner, null);
    }

    /** Refreshes the page; a day list moves to {@code day}, where the visit now is in the agenda. */
    private void refreshOwner(Refreshable owner, LocalDate day) {
        Refreshable target = owner != null ? owner : dialogOwner;
        if (target instanceof AbstractAppointmentListMb list) {
            list.showDay(day);
        } else if (target != null) {
            target.refresh();
        }
    }

    /** Id the lifecycle calls go to: the first open service of the visit. */
    private Integer actionId() {
        AppointmentVo a = visit.getFirstOpen();
        return a != null ? a.getId() : selected.getId();
    }

    // ------------------------------------------------------------------ actions (whole visit)

    /** Opens the start dialog: when did the visit start (now or the booked time, editable). */
    public void prepareStart(Refreshable owner) {
        dialogOwner = owner;
        prepareTime(false);
    }

    public void prepareStartFor(AppointmentVo row, Refreshable owner) {
        open(row);
        prepareStart(owner);
    }

    public void start(Refreshable owner) {
        if (selected == null) {
            return;
        }
        String path = "appointment/start/" + actionId();
        if (!isTimeUntouchedNow()) {
            path += "?date=" + timeDate + "&time=" + RestClient.encode(timeStart);
        }
        try {
            RestClient.post(path, null, AppointmentVo.class, userId(), locale());
            info(visit.isMulti() ? "op.msg.visitStarted" : "op.msg.started");
            reload();
            refreshOwner(owner, timeDate);
            PrimeFaces.current().executeScript("PF('apptStartDlg').hide();");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ when the visit happened

    /**
     * Businesses that register their visits at the end of the day keep the booked time; the others take
     * the current one. BOOKED is proposed when the booked visit is already over, NOW otherwise.
     */
    private void prepareTime(boolean forClose) {
        closing = forClose;
        timeMode = isBookedAvailable() && bookedEnd() != null && !bookedEnd().isAfter(nowDateTime()) ? "BOOKED" : "NOW";
        applyTimeMode();
    }

    /** Fills date / start / end with what the chosen option means (fired by the NOW / BOOKED buttons). */
    public void applyTimeMode() {
        if ("BOOKED".equals(timeMode) && isBookedAvailable()) {
            timeDate = LocalDate.parse(visit.getDate());
            timeStart = visit.getStartTime();
            timeEnd = visit.getEndTime();
            return;
        }
        timeMode = "NOW";
        LocalTime now = nowTime();
        timeDate = today();
        timeEnd = now.format(HHMM);
        // Closing: a visit started today keeps that start; otherwise it is placed so that it ends now.
        boolean startedToday = "IN_PROGRESS".equals(visit.getStatus()) && String.valueOf(today()).equals(visit.getDate());
        if (closing && startedToday && visit.getStartTime() != null) {
            timeStart = visit.getStartTime();
        } else if (closing) {
            int minutes = Math.max(0, now.getHour() * 60 + now.getMinute() - visit.getDurationMinutes());
            timeStart = LocalTime.of(minutes / 60, minutes % 60).format(HHMM);
        } else {
            timeStart = timeEnd;
        }
        proposedNow = timeDate + " " + timeStart + " " + timeEnd;
    }

    /** NOW left as proposed: nothing is sent and the backend records its own current time. */
    private boolean isTimeUntouchedNow() {
        return "NOW".equals(timeMode) && proposedNow != null
                && proposedNow.equals(timeDate + " " + timeStart + " " + timeEnd);
    }

    /** The booked time can be kept once it has passed (starting) or the visit was due to end (closing). */
    public boolean isBookedAvailable() {
        LocalTime t = parseTime(closing ? visit.getEndTime() : visit.getStartTime());
        if (visit.getDate() == null || t == null) {
            return false;
        }
        return !LocalDate.parse(visit.getDate()).atTime(t).isAfter(nowDateTime());
    }

    private java.time.LocalDateTime bookedEnd() {
        LocalTime t = parseTime(visit.getEndTime());
        return visit.getDate() != null && t != null ? LocalDate.parse(visit.getDate()).atTime(t) : null;
    }

    /** "Ahora · 16:42" */
    public String getNowLabel() {
        return sessionMb.text("op.when.now") + " \u00b7 " + nowTime().format(HHMM);
    }

    /** "Hora de la cita · 10:00" (today) or "Hora de la cita · 19/10/2026 10:00". */
    public String getBookedLabel() {
        String when = String.valueOf(today()).equals(visit.getDate())
                ? visit.getStartTime() : sessionMb.fmtDayTime(visit.getDate(), visit.getStartTime());
        return sessionMb.text("op.when.booked") + " \u00b7 " + when;
    }

    public LocalDate getMaxTimeDate() {
        return today();
    }

    private LocalTime nowTime() {
        return LocalTime.now(ZoneId.of(Constants.TIMEZONE)).withSecond(0).withNano(0);
    }

    private java.time.LocalDateTime nowDateTime() {
        return today().atTime(nowTime());
    }

    private static LocalTime parseTime(String hhmm) {
        try {
            return hhmm != null && hhmm.length() >= 5 ? LocalTime.parse(hhmm.substring(0, 5), HHMM) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void noShow(Refreshable owner) {
        if (selected == null) {
            return;
        }
        try {
            RestClient.post("appointment/noShow/" + actionId(), null, AppointmentVo.class, userId(), locale());
            info("op.msg.noShow");
            reload();
            refreshOwner(owner);
        } catch (RestException e) {
            errors(e);
        }
    }

    public void cancel(Refreshable owner) {
        if (selected == null) {
            return;
        }
        try {
            String reason = cancelReason != null ? cancelReason.trim() : "";
            RestClient.post("appointment/cancel/" + actionId() + "?reason=" + RestClient.encode(reason),
                    null, AppointmentVo.class, userId(), locale());
            info("op.msg.cancelled");
            cancelReason = null;
            PrimeFaces.current().executeScript("PF('apptCancelDlg').hide()");
            reload();
            refreshOwner(owner);
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Reassign every service of the visit still waiting to another professional. */
    public void reassign(Refreshable owner) {
        if (selected == null || reassignEmployeeId == null) {
            return;
        }
        try {
            for (AppointmentVo a : visit.getAppointments()) {
                if ("BOOKED".equals(a.getAppointmentStatus())) {
                    RestClient.post("appointment/reassign/" + a.getId() + "?employeeId=" + reassignEmployeeId,
                            null, AppointmentVo.class, userId(), locale());
                }
            }
            info("op.msg.reassigned");
            PrimeFaces.current().executeScript("PF('apptReassignDlg').hide()");
            reload();
            refreshOwner(owner);
        } catch (RestException e) {
            errors(e);
        }
    }

    public void savePreNotes() {
        if (selected == null) {
            return;
        }
        try {
            PreNotesVo vo = new PreNotesVo();
            vo.setPreNotes(selected.getPreNotes());
            RestClient.post("appointment/updatePreNotes/" + selected.getId(), vo, null, userId(), locale());
            info("msg.saved");
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Compose the REMINDER template, open wa.me, log it and mark the appointment as reminded. */
    public void reminder(Refreshable owner) {
        if (selected == null) {
            return;
        }
        try {
            MessageComposeVo compose = WhatsappSender.compose("REMINDER", selected.getCustomerId(),
                    selected.getId(), null, userId(), locale());
            if (compose == null || compose.getWaLink() == null) {
                errorKey("op.msg.noPhoneCustomer");
                return;
            }
            openInNewTab(compose.getWaLink());
            WhatsappSender.log(compose, selected.getId(), userId(), locale());
            try {
                RestClient.post("appointment/markReminderSent/" + selected.getId(), null, null, userId(), locale());
            } catch (RestException ignored) {
                // the message was already sent; the flag is cosmetic
            }
            info("op.msg.reminderSent");
            reload();
            refreshOwner(owner);
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Actions straight from a list row (no detail dialog open). */
    public void reminderFor(AppointmentVo row, Refreshable owner) {
        open(row);
        reminder(owner);
    }

    public void noShowFor(AppointmentVo row, Refreshable owner) {
        open(row);
        noShow(owner);
    }

    // ------------------------------------------------------------------ close (one charge for the visit)

    /** Prepare the charge: every open service of the visit done, every reserved product delivered. */
    public void prepareClose(Refreshable owner) {
        dialogOwner = owner;
        tipChoice = "0";
        tipCustomValue = null;
        tipCustomMode = "PERCENT";
        tipPaymentMethod = "CASH";
        if (selected == null) {
            return;
        }
        prepareTime(true);
        result = null;
        addServiceId = null;
        addProductId = null;
        closeForm = new CloseAppointmentVo();
        closeForm.setPostNotes(selected.getPostNotes());
        closeForm.setRecommendation(selected.getRecommendation());
        closeForm.setApplyReward(Boolean.TRUE.equals(selected.getRewardAvailable()));
        closeForm.setPaymentMethod(isAlreadyPaid()
                ? (selected.getPaymentMethod() != null ? selected.getPaymentMethod() : "ONLINE") : "CASH");
        closeServices = new ArrayList<>();
        closeItems = new ArrayList<>();
        for (AppointmentVo a : visit.getAppointments()) {
            if (!"BOOKED".equals(a.getAppointmentStatus()) && !"IN_PROGRESS".equals(a.getAppointmentStatus())) {
                continue;
            }
            CloseLine line = new CloseLine();
            line.appointmentId = a.getId();
            line.serviceId = a.getServiceId();
            line.name = a.getServiceName();
            line.employeeName = a.getEmployeeName();
            line.durationMinutes = a.getDurationMinutes();
            line.price = a.getServicePrice();
            closeServices.add(line);
            if (a.getItems() != null) {
                for (AppointmentItemVo it : a.getItems()) {
                    AppointmentItemVo copy = new AppointmentItemVo();
                    copy.setId(it.getId());
                    copy.setProductId(it.getProductId());
                    copy.setProductName(it.getProductName());
                    copy.setQuantity(it.getQuantity() != null ? it.getQuantity() : 1);
                    copy.setUnitPrice(it.getUnitPrice());
                    copy.setDelivered(Boolean.TRUE);
                    closeItems.add(copy);
                }
            }
        }
    }

    public void prepareCloseFor(AppointmentVo row, Refreshable owner) {
        open(row);
        prepareClose(owner);
    }

    /** Service picked in the "add service" dropdown: one more line, done and charged with the visit. */
    public void addService() {
        if (addServiceId == null) {
            return;
        }
        for (ServiceVo s : getServiceOptions()) {
            if (addServiceId.equals(s.getId())) {
                CloseLine line = new CloseLine();
                line.serviceId = s.getId();
                line.name = s.getName();
                line.employeeName = selected != null ? selected.getEmployeeName() : null;
                line.durationMinutes = s.getDurationMinutes();
                line.price = s.getPrice();
                line.extra = true;
                closeServices.add(line);
                break;
            }
        }
        addServiceId = null;
    }

    public void removeService(CloseLine line) {
        if (line != null && line.extra) {
            closeServices.remove(line);
        }
    }

    /** Products matching the typed text (name, barcode or reference), for the "add product" box. */
    public List<ProductVo> completeProduct(String query) {
        String q = norm(query);
        List<ProductVo> result = new ArrayList<>();
        if (q.isEmpty()) {
            return result;
        }
        for (ProductVo p : getProductOptions()) {
            String hay = norm(p.getName()) + " " + norm(p.getBarcode()) + " " + norm(p.getReferenceCode());
            if (hay.contains(q)) {
                result.add(p);
                if (result.size() >= 12) {
                    break;
                }
            }
        }
        return result;
    }

    /** Product picked in the autocomplete: one more line delivered now (or one more unit of it). */
    public void addProduct() {
        if (addProductId == null || addProductId.isBlank()) {
            return;
        }
        Integer id;
        try {
            id = Integer.valueOf(addProductId.trim());
        } catch (NumberFormatException e) {
            addProductId = null;
            return;
        }
        for (AppointmentItemVo it : closeItems) {
            if (it.getId() == null && id.equals(it.getProductId())) {
                it.setQuantity(nz(it.getQuantity()) + 1);
                addProductId = null;
                return;
            }
        }
        for (ProductVo p : getProductOptions()) {
            if (id.equals(p.getId())) {
                AppointmentItemVo it = new AppointmentItemVo();
                it.setProductId(p.getId());
                it.setProductName(p.getName());
                it.setQuantity(1);
                it.setUnitPrice(p.getPrice());
                it.setDelivered(Boolean.TRUE);
                closeItems.add(it);
                break;
            }
        }
        addProductId = null;
    }

    public void removeItem(AppointmentItemVo item) {
        if (item != null && item.getId() == null) {
            closeItems.remove(item);
        }
    }

    public boolean isAlreadyPaid() {
        return selected != null && "PAID".equalsIgnoreCase(selected.getPaymentStatus());
    }

    /** Sum of the services marked as done. */
    public BigDecimal getServiceTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CloseLine l : closeServices) {
            if (l.done && l.price != null) {
                sum = sum.add(l.price);
            }
        }
        return sum;
    }

    public int getServicesDone() {
        int n = 0;
        for (CloseLine l : closeServices) {
            if (l.done) {
                n++;
            }
        }
        return n;
    }

    public BigDecimal getProductsTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AppointmentItemVo it : closeItems) {
            if (Boolean.TRUE.equals(it.getDelivered()) && it.getUnitPrice() != null && it.getQuantity() != null) {
                sum = sum.add(it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
            }
        }
        return sum;
    }

    /** Known reward discount (frozen by the backend); zero until it is computed at close. */
    public BigDecimal getRewardTotal() {
        if (!Boolean.TRUE.equals(closeForm.getApplyReward()) || selected == null || selected.getRewardDiscount() == null) {
            return BigDecimal.ZERO;
        }
        return selected.getRewardDiscount();
    }

    public boolean isRewardPendingCalculation() {
        return Boolean.TRUE.equals(closeForm.getApplyReward()) && getRewardTotal().signum() == 0;
    }

    public BigDecimal getGrandTotal() {
        BigDecimal total = getServiceTotal().add(getProductsTotal()).subtract(getRewardTotal());
        return total.signum() < 0 ? BigDecimal.ZERO : total;
    }

    public int getDeliveredUnits() {
        int units = 0;
        for (AppointmentItemVo it : closeItems) {
            if (Boolean.TRUE.equals(it.getDelivered())) {
                units += nz(it.getQuantity());
            }
        }
        return units;
    }

    // ------------------------------------------------------------------ tip

    /** Percent of the tip, or null when it is a fixed amount / none. */
    private BigDecimal tipPercent() {
        if ("OTHER".equals(tipChoice)) {
            return "PERCENT".equals(tipCustomMode) ? tipCustomValue : null;
        }
        try {
            BigDecimal p = new BigDecimal(tipChoice);
            return p.signum() > 0 ? p : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Tip in money: a percent of what the customer pays for the visit, or the amount typed. */
    public BigDecimal getTipAmount() {
        BigDecimal pct = tipPercent();
        if (pct != null) {
            return getGrandTotal().multiply(pct).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
        if ("OTHER".equals(tipChoice) && "AMOUNT".equals(tipCustomMode) && tipCustomValue != null && tipCustomValue.signum() > 0) {
            return tipCustomValue.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public boolean isHasTip() {
        return getTipAmount().signum() > 0;
    }

    /** What the customer pays now: the visit plus the tip (only the tip when it was prepaid online). */
    public BigDecimal getAmountToCharge() {
        return (isAlreadyPaid() ? BigDecimal.ZERO : getGrandTotal()).add(getTipAmount());
    }

    /** Several employees in the visit: the tip is split among them by what each service charged. */
    public boolean isTipSplit() {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (CloseLine l : closeServices) {
            if (l.done && l.employeeName != null) {
                names.add(l.employeeName);
            }
        }
        return names.size() > 1;
    }

    public String getTipChoice() {
        return tipChoice;
    }

    public void setTipChoice(String tipChoice) {
        this.tipChoice = tipChoice != null ? tipChoice : "0";
    }

    public BigDecimal getTipCustomValue() {
        return tipCustomValue;
    }

    public void setTipCustomValue(BigDecimal tipCustomValue) {
        this.tipCustomValue = tipCustomValue;
    }

    public String getTipCustomMode() {
        return tipCustomMode;
    }

    public void setTipCustomMode(String tipCustomMode) {
        this.tipCustomMode = tipCustomMode;
    }

    public String getTipPaymentMethod() {
        return tipPaymentMethod;
    }

    public void setTipPaymentMethod(String tipPaymentMethod) {
        this.tipPaymentMethod = tipPaymentMethod;
    }

    /** "Cobrar $580.00" on the confirm button (the amount is what matters at the counter). */
    public String getChargeLabel() {
        return sessionMb.text("op.visit.charge").replace("{0}", sessionMb.fmtMoney(getAmountToCharge()));
    }

    public boolean isCanCharge() {
        return getServicesDone() > 0 || getDeliveredUnits() > 0;
    }

    /** POST appointment/close/{id} for the whole visit; on success the result dialog is shown by the page. */
    public void close(Refreshable owner) {
        if (selected == null) {
            return;
        }
        if (!isCanCharge()) {
            errorKey("op.visit.nothing");
            return;
        }
        try {
            List<AppointmentItemVo> items = new ArrayList<>();
            for (AppointmentItemVo it : closeItems) {
                AppointmentItemVo line = new AppointmentItemVo();
                line.setId(it.getId());
                line.setProductId(it.getProductId());
                line.setQuantity(it.getQuantity());
                line.setUnitPrice(it.getUnitPrice());
                line.setDelivered(Boolean.TRUE.equals(it.getDelivered()));
                items.add(line);
            }
            closeForm.setItems(items);
            closeForm.setWholeVisit(Boolean.TRUE);
            List<Integer> skipped = new ArrayList<>();
            List<Integer> extras = new ArrayList<>();
            for (CloseLine l : closeServices) {
                if (l.extra) {
                    if (l.done) {
                        extras.add(l.serviceId);
                    }
                } else if (!l.done) {
                    skipped.add(l.appointmentId);
                }
            }
            closeForm.setSkippedAppointmentIds(skipped);
            closeForm.setExtraServiceIds(extras);
            boolean untouched = isTimeUntouchedNow();
            closeForm.setVisitDate(untouched ? null : String.valueOf(timeDate));
            closeForm.setVisitStart(untouched ? null : timeStart);
            closeForm.setVisitEnd(untouched ? null : timeEnd);
            // Tip: the backend recomputes a percent on what it charges and splits it among the staff.
            BigDecimal tipPct = tipPercent();
            closeForm.setTipMode(isHasTip() ? (tipPct != null ? "PERCENT" : "AMOUNT") : null);
            closeForm.setTipValue(isHasTip() ? (tipPct != null ? tipPct : getTipAmount()) : null);
            closeForm.setTipPaymentMethod(isAlreadyPaid() ? tipPaymentMethod : closeForm.getPaymentMethod());
            result = RestClient.post("appointment/close/" + actionId(), closeForm, CloseResultVo.class,
                    userId(), locale());
            info(result != null && result.getServicesClosed() != null && result.getServicesClosed() > 1
                    ? "op.msg.visitClosed" : "op.msg.closed");
            if (result != null && result.getAppointment() != null) {
                selected = result.getAppointment();
                visit = VisitRow.of(selected);
            }
            refreshOwner(owner, timeDate);
            PrimeFaces.current().executeScript("PF('apptCloseDlg').hide();PF('apptDetailDlg').hide();PF('apptResultDlg').show();");
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Opens the review request built by the close transaction and logs it. */
    public void askReview() {
        if (result == null || result.getReviewMessage() == null || result.getReviewMessage().getWaLink() == null) {
            errorKey("op.msg.noPhoneCustomer");
            return;
        }
        openInNewTab(result.getReviewMessage().getWaLink());
        WhatsappSender.log(result.getReviewMessage(),
                result.getAppointment() != null ? result.getAppointment().getId() : null, userId(), locale());
        info("op.msg.messageSent");
    }

    // ------------------------------------------------------------------ catalogs for the counter

    /** Services of the business (bookable ones first), for "add service" at the charge. */
    public List<ServiceVo> getServiceOptions() {
        if (serviceOptions == null) {
            serviceOptions = new ArrayList<>();
            try {
                List<ServiceVo> list = RestClient.get("service/getServices", SERVICE_LIST, userId(), locale());
                if (list != null) {
                    for (ServiceVo s : list) {
                        if (s.getStatus() == null || s.getStatus() == 1) {
                            serviceOptions.add(s);
                        }
                    }
                }
            } catch (RestException e) {
                errors(e);
            }
        }
        return serviceOptions;
    }

    private List<ProductVo> getProductOptions() {
        if (productOptions == null) {
            productOptions = new ArrayList<>();
            try {
                List<ProductVo> list = RestClient.get("product/getProducts?includeImages=false", PRODUCT_LIST,
                        userId(), locale());
                if (list != null) {
                    productOptions = list;
                }
            } catch (RestException e) {
                errors(e);
            }
        }
        return productOptions;
    }

    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    // ------------------------------------------------------------------ state helpers (visit)

    public VisitRow getVisit() {
        return visit;
    }

    public boolean isMultiVisit() {
        return visit.isMulti();
    }

    public boolean isCanStart() {
        return selected != null && visit.isAnyBooked();
    }

    public boolean isCanClose() {
        return selected != null && visit.isAnyOpen();
    }

    public boolean isCanCancel() {
        return isCanClose();
    }

    public boolean isCanNoShow() {
        return selected != null && visit.isAllWaiting();
    }

    public boolean isCanReassign() {
        return sessionMb.isAdmin() && isCanNoShow();
    }

    public boolean isCanRemind() {
        return isCanStart() && selected.getCustomerPhone() != null;
    }

    /** Row-level helpers for the lists (same rules, on any visit row). */
    public boolean canStartVisit(VisitRow v) {
        return v != null && v.isAnyBooked();
    }

    public boolean canCloseVisit(VisitRow v) {
        return v != null && v.isAnyOpen();
    }

    public boolean canNoShowVisit(VisitRow v) {
        return v != null && v.isAllWaiting();
    }

    public List<EmployeeVo> getEmployees() {
        if (employees == null) {
            employees = new ArrayList<>();
            if (sessionMb.isAdmin()) {
                try {
                    List<EmployeeVo> list = RestClient.get("employee/getEmployees", EMPLOYEE_LIST, userId(), locale());
                    if (list != null) {
                        employees = list;
                    }
                } catch (RestException e) {
                    errors(e);
                }
            }
        }
        return employees;
    }

    /** Reserved products of the whole visit. */
    public int getProductsCount() {
        return visit.getItemsCount();
    }

    public boolean isHasPreviousVisit() {
        return selected != null && (selected.getPreviousVisitDate() != null
                || (selected.getPreviousPostNotes() != null && !selected.getPreviousPostNotes().isBlank()));
    }

    // ------------------------------------------------------------------ getters / setters

    public String getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(String timeMode) {
        this.timeMode = timeMode;
    }

    public LocalDate getTimeDate() {
        return timeDate;
    }

    public void setTimeDate(LocalDate timeDate) {
        this.timeDate = timeDate;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public AppointmentVo getSelected() {
        return selected;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Integer getReassignEmployeeId() {
        return reassignEmployeeId;
    }

    public void setReassignEmployeeId(Integer reassignEmployeeId) {
        this.reassignEmployeeId = reassignEmployeeId;
    }

    public CloseAppointmentVo getCloseForm() {
        return closeForm;
    }

    public List<CloseLine> getCloseServices() {
        return closeServices;
    }

    public List<AppointmentItemVo> getCloseItems() {
        return closeItems;
    }

    public CloseResultVo getResult() {
        return result;
    }

    public Integer getAddServiceId() {
        return addServiceId;
    }

    public void setAddServiceId(Integer addServiceId) {
        this.addServiceId = addServiceId;
    }

    public String getAddProductId() {
        return addProductId;
    }

    public void setAddProductId(String addProductId) {
        this.addProductId = addProductId;
    }
}
