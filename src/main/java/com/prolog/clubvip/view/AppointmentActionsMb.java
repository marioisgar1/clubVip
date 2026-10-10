package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AppointmentItemVo;
import com.prolog.clubvip.vo.AppointmentVo;
import com.prolog.clubvip.vo.CloseAppointmentVo;
import com.prolog.clubvip.vo.CloseResultVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.MessageComposeVo;
import com.prolog.clubvip.vo.PreNotesVo;

import org.primefaces.PrimeFaces;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Detail + lifecycle actions of one appointment (start, close with charge, cancel, no-show,
 * reassign, WhatsApp reminder). Backs /WEB-INF/includes/appointmentDialogs.xhtml, which the
 * schedule, day and "my appointments" pages include; the owning page is passed to each action
 * so its list is refreshed afterwards.
 */
@Named("appointmentActionsMb")
@ViewScoped
public class AppointmentActionsMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();

    private AppointmentVo selected;
    private String cancelReason;
    private Integer reassignEmployeeId;
    private List<EmployeeVo> employees;

    // close form
    private CloseAppointmentVo closeForm = new CloseAppointmentVo();
    private List<AppointmentItemVo> closeItems = new ArrayList<>();
    private CloseResultVo result;

    // ------------------------------------------------------------------ detail

    /** Load the full appointment (expediente, products, previous visit) and keep it as selected. */
    public void open(AppointmentVo row) {
        selected = null;
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
        if (owner != null) {
            owner.refresh();
        }
    }

    // ------------------------------------------------------------------ actions

    public void start(Refreshable owner) {
        if (selected == null) {
            return;
        }
        try {
            RestClient.post("appointment/start/" + selected.getId(), null, AppointmentVo.class, userId(), locale());
            info("op.msg.started");
            reload();
            refreshOwner(owner);
        } catch (RestException e) {
            errors(e);
        }
    }

    public void noShow(Refreshable owner) {
        if (selected == null) {
            return;
        }
        try {
            RestClient.post("appointment/noShow/" + selected.getId(), null, AppointmentVo.class, userId(), locale());
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
            RestClient.post("appointment/cancel/" + selected.getId() + "?reason=" + RestClient.encode(reason),
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

    public void reassign(Refreshable owner) {
        if (selected == null || reassignEmployeeId == null) {
            return;
        }
        try {
            RestClient.post("appointment/reassign/" + selected.getId() + "?employeeId=" + reassignEmployeeId,
                    null, AppointmentVo.class, userId(), locale());
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

    /** Reminder straight from a table row (no detail dialog open). */
    public void reminderFor(AppointmentVo row, Refreshable owner) {
        open(row);
        reminder(owner);
    }

    public void startFor(AppointmentVo row, Refreshable owner) {
        open(row);
        start(owner);
    }

    public void noShowFor(AppointmentVo row, Refreshable owner) {
        open(row);
        noShow(owner);
    }

    // ------------------------------------------------------------------ close

    /** Prepare the close form from the selected appointment (all reserved products delivered by default). */
    public void prepareClose() {
        if (selected == null) {
            return;
        }
        result = null;
        closeForm = new CloseAppointmentVo();
        closeForm.setPostNotes(selected.getPostNotes());
        closeForm.setRecommendation(selected.getRecommendation());
        closeForm.setApplyReward(Boolean.TRUE.equals(selected.getRewardAvailable()));
        if (isAlreadyPaid()) {
            closeForm.setPaymentMethod(selected.getPaymentMethod() != null ? selected.getPaymentMethod() : "ONLINE");
        } else {
            closeForm.setPaymentMethod("CASH");
        }
        closeItems = new ArrayList<>();
        if (selected.getItems() != null) {
            for (AppointmentItemVo it : selected.getItems()) {
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

    public void prepareCloseFor(AppointmentVo row) {
        open(row);
        prepareClose();
    }

    public boolean isAlreadyPaid() {
        return selected != null && "PAID".equalsIgnoreCase(selected.getPaymentStatus());
    }

    public BigDecimal getServiceTotal() {
        return selected != null && selected.getServicePrice() != null ? selected.getServicePrice() : BigDecimal.ZERO;
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

    /** POST appointment/close/{id}; on success the result dialog is shown by the page (oncomplete). */
    public void close(Refreshable owner) {
        if (selected == null) {
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
            result = RestClient.post("appointment/close/" + selected.getId(), closeForm, CloseResultVo.class,
                    userId(), locale());
            info("op.msg.closed");
            if (result != null && result.getAppointment() != null) {
                selected = result.getAppointment();
            }
            refreshOwner(owner);
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

    // ------------------------------------------------------------------ state helpers

    public boolean isCanStart() {
        return selected != null && "BOOKED".equals(selected.getAppointmentStatus());
    }

    public boolean isCanClose() {
        return selected != null && ("BOOKED".equals(selected.getAppointmentStatus())
                || "IN_PROGRESS".equals(selected.getAppointmentStatus()));
    }

    public boolean isCanCancel() {
        return isCanClose();
    }

    public boolean isCanNoShow() {
        return isCanStart();
    }

    public boolean isCanReassign() {
        return sessionMb.isAdmin() && isCanStart();
    }

    public boolean isCanRemind() {
        return isCanStart() && selected.getCustomerPhone() != null;
    }

    /** Row-level helpers for the tables (same rules, on any row). */
    public boolean canStart(AppointmentVo a) {
        return a != null && "BOOKED".equals(a.getAppointmentStatus());
    }

    public boolean canClose(AppointmentVo a) {
        return a != null && ("BOOKED".equals(a.getAppointmentStatus()) || "IN_PROGRESS".equals(a.getAppointmentStatus()));
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

    public int getProductsCount() {
        return selected != null && selected.getItems() != null ? selected.getItems().size() : 0;
    }

    public boolean isHasPreviousVisit() {
        return selected != null && (selected.getPreviousVisitDate() != null
                || (selected.getPreviousPostNotes() != null && !selected.getPreviousPostNotes().isBlank()));
    }

    // ------------------------------------------------------------------ getters / setters

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

    public List<AppointmentItemVo> getCloseItems() {
        return closeItems;
    }

    public CloseResultVo getResult() {
        return result;
    }
}
