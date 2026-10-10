package com.prolog.clubvip.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.CreditInstallmentVo;
import com.prolog.clubvip.vo.CreditPortfolioVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Overdue portfolio (REPORT_PORTFOLIO): what has to be collected as of a date, already late,
 * falling that same day, and what is still ahead. credit/getPortfolio?asOf=
 */
@Named("reportPortfolioMb")
@ViewScoped
public class ReportPortfolioMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;

    private Date asOf;
    /** Narrows the table to what actually has to be chased on the reference day. */
    private boolean onlyDue = true;
    private CreditPortfolioVo portfolio = new CreditPortfolioVo();

    @PostConstruct
    public void init() {
        asOf = toDate(today());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            CreditPortfolioVo p = RestClient.get("credit/getPortfolio?asOf=" + isoDay(asOf != null ? asOf : new Date()),
                    CreditPortfolioVo.class, userId(), locale());
            portfolio = p != null ? p : new CreditPortfolioVo();
        } catch (RestException e) {
            portfolio = new CreditPortfolioVo();
            errors(e);
        }
    }

    /** Rows to render, honouring the "overdue and today only" switch. */
    public List<CreditInstallmentVo> getLines() {
        List<CreditInstallmentVo> all = portfolio != null && portfolio.getLines() != null
                ? portfolio.getLines() : new ArrayList<>();
        if (!onlyDue) {
            return all;
        }
        List<CreditInstallmentVo> filtered = new ArrayList<>();
        for (CreditInstallmentVo line : all) {
            if (Boolean.TRUE.equals(line.getOverdue()) || isReferenceDay(line)) {
                filtered.add(line);
            }
        }
        return filtered;
    }

    private boolean isReferenceDay(CreditInstallmentVo line) {
        Date reference = portfolio != null && portfolio.getAsOf() != null ? portfolio.getAsOf() : asOf;
        return reference != null && line.getDueDate() != null && isoDay(reference).equals(isoDay(line.getDueDate()));
    }

    public String reminderLink(CreditInstallmentVo line) {
        if (line == null) {
            return null;
        }
        String digits = waDigits(line.getCustomerPhone());
        if (digits.isEmpty()) {
            return null;
        }
        String text = sessionMb.text("pos.credit.wa.reminder")
                .replace("{0}", line.getCustomerName() != null ? line.getCustomerName() : "")
                .replace("{1}", sessionMb.fmtMoney(line.getPendingAmount()))
                .replace("{2}", fmtDay(line.getDueDate()))
                .replace("{3}", sessionMb.getBusinessName());
        return WhatsAppLink.withText(digits, text);
    }

    // ------------------------------------------------------------------ getters / setters

    public Date getAsOf() {
        return asOf;
    }

    public void setAsOf(Date asOf) {
        this.asOf = asOf;
    }

    public boolean isOnlyDue() {
        return onlyDue;
    }

    public void setOnlyDue(boolean onlyDue) {
        this.onlyDue = onlyDue;
    }

    public CreditPortfolioVo getPortfolio() {
        return portfolio;
    }
}
