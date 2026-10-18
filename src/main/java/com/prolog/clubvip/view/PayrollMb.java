package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.CommissionVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.GeneratePayrollVo;
import com.prolog.clubvip.vo.PayrollPeriodVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Staff payroll: current-period preview per employee, frozen periods ("cortes") and pending commissions.
 */
@Named("payrollMb")
@ViewScoped
public class PayrollMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type EMPLOYEE_LIST = new TypeToken<List<EmployeeVo>>() { }.getType();
    private static final Type PERIOD_LIST = new TypeToken<List<PayrollPeriodVo>>() { }.getType();
    private static final Type COMMISSION_LIST = new TypeToken<List<CommissionVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<EmployeeVo> employees = new ArrayList<>();
    private Integer selectedEmployeeId; // null = all
    private List<PayrollPeriodVo> previews = new ArrayList<>();
    private List<PayrollPeriodVo> periods = new ArrayList<>();
    private List<PayrollPeriodVo> filteredPeriods;
    private String payStatusFilter; // null = all
    private List<CommissionVo> pendingCommissions = new ArrayList<>();
    private PayrollPeriodVo detail = new PayrollPeriodVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<EmployeeVo> list = RestClient.get("employee/getEmployees", EMPLOYEE_LIST, userId(), locale());
            employees = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            employees = new ArrayList<>();
            errors(e);
        }
        loadPreviews();
        loadPeriods();
        loadPending();
    }

    // ------------------------------------------------------------------ preview

    public void loadPreviews() {
        previews = new ArrayList<>();
        for (EmployeeVo emp : employees) {
            if (selectedEmployeeId != null && !selectedEmployeeId.equals(emp.getId())) {
                continue;
            }
            try {
                PayrollPeriodVo p = RestClient.get("payroll/preview?employeeId=" + emp.getId(),
                        PayrollPeriodVo.class, userId(), locale());
                if (p != null) {
                    if (p.getEmployeeId() == null) {
                        p.setEmployeeId(emp.getId());
                    }
                    if (p.getEmployeeName() == null) {
                        p.setEmployeeName(emp.getFullName());
                    }
                    if (p.getPayStatus() == null) {
                        p.setPayStatus("PENDING");
                    }
                    fillPhoto(p);
                    previews.add(p);
                }
            } catch (RestException e) {
                errors(e);
            }
        }
    }

    public void generate(PayrollPeriodVo preview) {
        GeneratePayrollVo req = new GeneratePayrollVo();
        req.setEmployeeId(preview.getEmployeeId());
        req.setPeriodStart(preview.getPeriodStart());
        req.setPeriodEnd(preview.getPeriodEnd());
        RestResponse r = RestClient.post("payroll/generatePeriod", req, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.payroll.generated"));
            loadPreviews();
            loadPeriods();
            loadPending();
        } else {
            errors(r);
        }
    }

    /** Period caption of the previews: "dd/MM/yyyy - dd/MM/yyyy" (earliest start, latest end). */
    public String getPeriodLabel() {
        String start = null;
        String end = null;
        for (PayrollPeriodVo p : previews) {
            if (p.getPeriodStart() != null && (start == null || p.getPeriodStart().compareTo(start) < 0)) {
                start = p.getPeriodStart();
            }
            if (p.getPeriodEnd() != null && (end == null || p.getPeriodEnd().compareTo(end) > 0)) {
                end = p.getPeriodEnd();
            }
        }
        if (start == null && end == null) {
            return sessionMb.getTodayLabel();
        }
        return sessionMb.fmtDay(start) + " - " + sessionMb.fmtDay(end);
    }

    public BigDecimal getPreviewTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayrollPeriodVo p : previews) {
            if (p.getTotalAmount() != null) {
                sum = sum.add(p.getTotalAmount());
            }
        }
        return sum;
    }

    public BigDecimal getPreviewCommission() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayrollPeriodVo p : previews) {
            if (p.getCommissionAmount() != null) {
                sum = sum.add(p.getCommissionAmount());
            }
        }
        return sum;
    }

    /** Tips owed in the previewed cuts (100 % for the staff; included in the total to pay). */
    public BigDecimal getPreviewTip() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayrollPeriodVo p : previews) {
            if (p.getTipAmount() != null) {
                sum = sum.add(p.getTipAmount());
            }
        }
        return sum;
    }

    public BigDecimal getPreviewSalary() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayrollPeriodVo p : previews) {
            if (p.getSalaryAmount() != null) {
                sum = sum.add(p.getSalaryAmount());
            }
        }
        return sum;
    }

    /** Total of frozen periods not yet paid. */
    public BigDecimal getPendingTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (PayrollPeriodVo p : periods) {
            if (!"PAID".equals(p.getPayStatus()) && p.getTotalAmount() != null) {
                sum = sum.add(p.getTotalAmount());
            }
        }
        return sum;
    }

    // ------------------------------------------------------------------ periods

    public void loadPeriods() {
        StringBuilder path = new StringBuilder("payroll/getPeriods?");
        if (selectedEmployeeId != null) {
            path.append("employeeId=").append(selectedEmployeeId).append('&');
        }
        if (payStatusFilter != null && !payStatusFilter.isBlank()) {
            path.append("payStatus=").append(RestClient.encode(payStatusFilter));
        }
        try {
            List<PayrollPeriodVo> list = RestClient.get(path.toString(), PERIOD_LIST, userId(), locale());
            periods = list != null ? list : new ArrayList<>();
            periods.forEach(this::fillPhoto);
        } catch (RestException e) {
            periods = new ArrayList<>();
            errors(e);
        }
    }

    public void showDetail(PayrollPeriodVo period) {
        detail = period != null ? period : new PayrollPeriodVo();
        PrimeFaces.current().executeScript("PF('detailDialog').show()");
    }

    public void markPaid(PayrollPeriodVo period) {
        RestResponse r = RestClient.post("payroll/markPaid/" + period.getId(), null, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.payroll.paid"));
            loadPeriods();
        } else {
            errors(r);
        }
    }

    // ------------------------------------------------------------------ pending commissions

    public void loadPending() {
        String path = "payroll/getPendingCommissions" + (selectedEmployeeId != null ? "?employeeId=" + selectedEmployeeId : "");
        try {
            List<CommissionVo> list = RestClient.get(path, COMMISSION_LIST, userId(), locale());
            pendingCommissions = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            pendingCommissions = new ArrayList<>();
            errors(e);
        }
    }

    public BigDecimal getPendingCommissionTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CommissionVo c : pendingCommissions) {
            if (c.getCommissionAmount() != null) {
                sum = sum.add(c.getCommissionAmount());
            }
        }
        return sum;
    }

    /** Employee filter changed: reload every section. */
    public void onEmployeeChange() {
        loadPreviews();
        loadPeriods();
        loadPending();
    }

    public String statusBadge(String payStatus) {
        if ("PAID".equals(payStatus)) {
            return "badge badge-success";
        }
        return "badge badge-warning";
    }

    // ------------------------------------------------------------------ helpers

    /** The payroll endpoints don't say whether the employee has a photo: take it from the staff list. */
    private void fillPhoto(PayrollPeriodVo p) {
        if (Boolean.TRUE.equals(p.getEmployeeHasPhoto()) || p.getEmployeeId() == null) {
            return;
        }
        for (EmployeeVo emp : employees) {
            if (p.getEmployeeId().equals(emp.getId())) {
                p.setEmployeeHasPhoto(Boolean.TRUE.equals(emp.getHasPhoto()));
                return;
            }
        }
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

    public List<EmployeeVo> getEmployees() {
        return employees;
    }

    public Integer getSelectedEmployeeId() {
        return selectedEmployeeId;
    }

    public void setSelectedEmployeeId(Integer selectedEmployeeId) {
        this.selectedEmployeeId = selectedEmployeeId;
    }

    public List<PayrollPeriodVo> getPreviews() {
        return previews;
    }

    public List<PayrollPeriodVo> getPeriods() {
        return periods;
    }

    public List<PayrollPeriodVo> getFilteredPeriods() {
        return filteredPeriods;
    }

    public void setFilteredPeriods(List<PayrollPeriodVo> filteredPeriods) {
        this.filteredPeriods = filteredPeriods;
    }

    public String getPayStatusFilter() {
        return payStatusFilter;
    }

    public void setPayStatusFilter(String payStatusFilter) {
        this.payStatusFilter = payStatusFilter;
    }

    public List<CommissionVo> getPendingCommissions() {
        return pendingCommissions;
    }

    public PayrollPeriodVo getDetail() {
        return detail;
    }
}
