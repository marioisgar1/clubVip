package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CreditInstallmentVo;
import com.prolog.clubvip.vo.CreditPaymentVo;
import com.prolog.clubvip.vo.CreditPlanVo;
import com.prolog.clubvip.vo.PaymentMethodVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Collections (COLLECTION_VIEW): the credits sold, what is still owed on each one, the detail
 * with instalments and payment history, the dialog that registers a collection (full or partial,
 * or a loose payment spread over the oldest dues) and the undo of a wrong collection.
 * credit/getCreditPlans?planStatus= · credit/getCreditPlan/{id} · credit/registerPayment ·
 * credit/cancelPayment/{id} · paymentMethod/getPaymentMethods
 */
@Named("collectionMb")
@ViewScoped
public class CollectionMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type PLAN_LIST_TYPE = new TypeToken<List<CreditPlanVo>>() { }.getType();
    private static final Type METHOD_LIST_TYPE = new TypeToken<List<PaymentMethodVo>>() { }.getType();

    /** OPEN by default: the seller cares about what is still owed. */
    private String planStatus = "OPEN";
    private List<CreditPlanVo> plans = new ArrayList<>();
    private List<CreditPlanVo> filtered;
    private List<PaymentMethodVo> collectionMethods = new ArrayList<>();

    private CreditPlanVo selected;
    /** Instalment being collected; null means a loose payment over the oldest dues. */
    private CreditInstallmentVo selectedInstallment;
    private BigDecimal paymentAmount;
    private Integer paymentMethodId;
    private String paymentNotes;

    @PostConstruct
    public void init() {
        loadPaymentMethods();
        refresh();
    }

    @Override
    public void refresh() {
        try {
            String path = "credit/getCreditPlans";
            if (planStatus != null && !planStatus.isEmpty()) {
                path = path + "?planStatus=" + RestClient.encode(planStatus);
            }
            List<CreditPlanVo> list = RestClient.get(path, PLAN_LIST_TYPE, userId(), locale());
            plans = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            plans = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    private void loadPaymentMethods() {
        try {
            List<PaymentMethodVo> all = RestClient.get("paymentMethod/getPaymentMethods", METHOD_LIST_TYPE, userId(), locale());
            collectionMethods = new ArrayList<>();
            if (all != null) {
                for (PaymentMethodVo method : all) {
                    // Paying off a credit with the credit method itself makes no sense.
                    if (!Boolean.TRUE.equals(method.getCredit())) {
                        collectionMethods.add(method);
                    }
                }
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Opens the detail of a plan: instalments, sale lines and collection history. */
    public void openPlan(CreditPlanVo plan) {
        if (plan == null || plan.getId() == null) {
            return;
        }
        try {
            CreditPlanVo full = RestClient.get("credit/getCreditPlan/" + plan.getId(), CreditPlanVo.class, userId(), locale());
            selected = full != null ? full : plan;
        } catch (RestException e) {
            selected = plan;
            errors(e);
        }
    }

    public void prepareCollect(CreditInstallmentVo installment) {
        selectedInstallment = installment;
        paymentAmount = installment != null ? installment.getPendingAmount()
                : (selected != null ? selected.getBalance() : null);
        paymentMethodId = collectionMethods.size() == 1 ? collectionMethods.get(0).getId() : null;
        paymentNotes = null;
    }

    /** Loose payment: the backend spreads it over the oldest pending instalments. */
    public void prepareFreePayment() {
        prepareCollect(null);
    }

    public void collect() {
        if (selected == null) {
            return;
        }
        if (paymentAmount == null || paymentAmount.signum() <= 0) {
            errorKey("pos.credit.amountRequired");
            return;
        }
        CreditPaymentVo payment = new CreditPaymentVo();
        payment.setCreditPlanId(selected.getId());
        payment.setCreditInstallmentId(selectedInstallment != null ? selectedInstallment.getId() : null);
        payment.setAmount(paymentAmount);
        payment.setPaymentMethodId(paymentMethodId);
        payment.setNotes(paymentNotes);
        try {
            RestClient.post("credit/registerPayment", payment, null, userId(), locale());
            openPlan(selected); // re-read: the backend just set the instalment states
            refresh();
            info("pos.credit.collected");
            PrimeFaces.current().executeScript("PF('collectDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Undo a collection registered by mistake: the instalments it covered open again. */
    public void undoPayment(CreditPaymentVo payment) {
        if (payment == null || payment.getId() == null || selected == null) {
            return;
        }
        try {
            RestClient.post("credit/cancelPayment/" + payment.getId(), null, null, userId(), locale());
            openPlan(selected);
            refresh();
            info("pos.credit.paymentUndone");
        } catch (RestException e) {
            errors(e);
        }
    }

    /** wa.me reminder for the customer of the opened plan, with its next due or overdue amount. */
    public String reminderLink(CreditPlanVo plan) {
        if (plan == null) {
            return null;
        }
        String digits = waDigits(plan.getCustomerPhone());
        if (digits.isEmpty()) {
            return null;
        }
        BigDecimal amount = plan.getOverdueAmount() != null && plan.getOverdueAmount().signum() > 0
                ? plan.getOverdueAmount() : plan.getNextDueAmount();
        String text = sessionMb.text("pos.credit.wa.reminder")
                .replace("{0}", plan.getCustomerName() != null ? plan.getCustomerName() : "")
                .replace("{1}", sessionMb.fmtMoney(amount))
                .replace("{2}", fmtDay(plan.getNextDueDate()))
                .replace("{3}", sessionMb.getBusinessName());
        return WhatsAppLink.withText(digits, text);
    }

    // --- totals shown on the header ---------------------------------------

    public BigDecimal getTotalOwed() {
        BigDecimal total = BigDecimal.ZERO;
        for (CreditPlanVo plan : plans) {
            if ("OPEN".equals(plan.getPlanStatus()) && plan.getBalance() != null) {
                total = total.add(plan.getBalance());
            }
        }
        return total;
    }

    public BigDecimal getTotalOverdue() {
        BigDecimal total = BigDecimal.ZERO;
        for (CreditPlanVo plan : plans) {
            if ("OPEN".equals(plan.getPlanStatus()) && plan.getOverdueAmount() != null) {
                total = total.add(plan.getOverdueAmount());
            }
        }
        return total;
    }

    public int getOpenCount() {
        int n = 0;
        for (CreditPlanVo plan : plans) {
            if ("OPEN".equals(plan.getPlanStatus())) {
                n++;
            }
        }
        return n;
    }

    public int getOverdueCustomers() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (CreditPlanVo plan : plans) {
            if ("OPEN".equals(plan.getPlanStatus()) && nz(plan.getOverdueCount()) > 0 && plan.getCustomerId() != null) {
                ids.add(plan.getCustomerId());
            }
        }
        return ids.size();
    }

    public String planBadge(String status) {
        if ("PAID".equals(status)) {
            return "badge badge-success";
        }
        if ("CANCELLED".equals(status)) {
            return "badge badge-danger";
        }
        return "badge badge-warning";
    }

    public String installmentBadge(CreditInstallmentVo i) {
        if (i == null) {
            return "badge badge-muted";
        }
        if ("PAID".equals(i.getInstallmentStatus())) {
            return "badge badge-success";
        }
        if (Boolean.TRUE.equals(i.getOverdue())) {
            return "badge badge-danger";
        }
        if ("PARTIAL".equals(i.getInstallmentStatus())) {
            return "badge badge-info";
        }
        return "badge badge-muted";
    }

    public String installmentLabel(CreditInstallmentVo i) {
        if (i == null) {
            return "";
        }
        if ("PAID".equals(i.getInstallmentStatus())) {
            return sessionMb.text("pos.credit.status.paid");
        }
        if (Boolean.TRUE.equals(i.getOverdue())) {
            return sessionMb.text("pos.credit.status.overdue") + " (" + nz(i.getDaysOverdue()) + ")";
        }
        if ("PARTIAL".equals(i.getInstallmentStatus())) {
            return sessionMb.text("pos.credit.status.partial");
        }
        return sessionMb.text("pos.credit.status.pending");
    }

    public String frequencyLabel(String code) {
        return code != null ? sessionMb.text("pos.credit.frequency." + code) : "";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public List<CreditPlanVo> getPlans() {
        return plans;
    }

    public List<CreditPlanVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<CreditPlanVo> filtered) {
        this.filtered = filtered;
    }

    public List<PaymentMethodVo> getCollectionMethods() {
        return collectionMethods;
    }

    public CreditPlanVo getSelected() {
        return selected;
    }

    public CreditInstallmentVo getSelectedInstallment() {
        return selectedInstallment;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentNotes() {
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }
}
