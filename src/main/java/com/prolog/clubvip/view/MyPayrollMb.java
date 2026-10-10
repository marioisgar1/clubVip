package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CommissionVo;
import com.prolog.clubvip.vo.EmployeePayrollVo;
import com.prolog.clubvip.vo.EmployeeVo;
import com.prolog.clubvip.vo.PayrollPeriodVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Employee view of their own payroll: pay scheme, current period and history.
 */
@Named("myPayrollMb")
@ViewScoped
public class MyPayrollMb implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionMb sessionMb;

    private EmployeePayrollVo payroll = new EmployeePayrollVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            EmployeePayrollVo p = RestClient.get("payroll/getMyPayroll", EmployeePayrollVo.class,
                    sessionMb.getUserId(), sessionMb.getLocale());
            payroll = p != null ? p : new EmployeePayrollVo();
        } catch (RestException e) {
            payroll = new EmployeePayrollVo();
            for (String m : e.getMessages()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
            }
        }
    }

    public EmployeeVo getEmployee() {
        return payroll.getEmployee() != null ? payroll.getEmployee() : new EmployeeVo();
    }

    public PayrollPeriodVo getCurrent() {
        return payroll.getCurrent() != null ? payroll.getCurrent() : new PayrollPeriodVo();
    }

    public List<PayrollPeriodVo> getPeriods() {
        return payroll.getPeriods() != null ? payroll.getPeriods() : new ArrayList<>();
    }

    public List<CommissionVo> getCommissions() {
        return getCurrent().getCommissions() != null ? getCurrent().getCommissions() : new ArrayList<>();
    }

    public String getCurrentPeriodLabel() {
        PayrollPeriodVo c = getCurrent();
        if (c.getPeriodStart() == null && c.getPeriodEnd() == null) {
            return "";
        }
        return sessionMb.fmtDay(c.getPeriodStart()) + " - " + sessionMb.fmtDay(c.getPeriodEnd());
    }

    public String statusBadge(String payStatus) {
        return "PAID".equals(payStatus) ? "badge badge-success" : "badge badge-warning";
    }

    public EmployeePayrollVo getPayroll() {
        return payroll;
    }
}
