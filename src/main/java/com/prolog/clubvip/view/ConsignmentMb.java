package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessSummaryVo;
import com.prolog.clubvip.vo.ConsignmentAgreementVo;
import com.prolog.clubvip.vo.ConsignmentInviteVo;
import com.prolog.clubvip.vo.ConsignmentSummaryVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Consignment hub (CONSIGNMENT_VIEW): invitations to answer, stores that sell my products and
 * suppliers whose products I sell, plus the invite wizard. consignment/getSummary ·
 * consignment/searchSuppliers · consignment/invite · consignment/cancelInvitation/{id} ·
 * consignment/accept/{id} · consignment/reject/{id}
 */
@Named("consignmentMb")
@ViewScoped
public class ConsignmentMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type SUPPLIER_LIST = new TypeToken<List<BusinessSummaryVo>>() { }.getType();
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private ConsignmentSummaryVo summary = new ConsignmentSummaryVo();

    // invite wizard
    private String searchText;
    private List<BusinessSummaryVo> candidates = new ArrayList<>();
    private BusinessSummaryVo chosen;
    private BigDecimal commission;
    private String frequency;
    private String message;

    // reject dialog
    private ConsignmentAgreementVo answering;
    private String rejectReason;

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            ConsignmentSummaryVo s = RestClient.get("consignment/getSummary", ConsignmentSummaryVo.class, userId(), locale());
            summary = s != null ? s : new ConsignmentSummaryVo();
        } catch (RestException e) {
            summary = new ConsignmentSummaryVo();
            errors(e);
        }
    }

    // ------------------------------------------------------------------ supplier side

    public void accept(ConsignmentAgreementVo a) {
        try {
            RestClient.post("consignment/accept/" + a.getId(), null, null, userId(), locale());
            // Accepting switches the module on: keep the session flag in step without a new login.
            sessionMb.getUser().setModuleConsignment(Boolean.TRUE);
            info("cons.msg.accepted");
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void prepareReject(ConsignmentAgreementVo a) {
        answering = a;
        rejectReason = null;
    }

    public void reject() {
        if (answering == null) {
            return;
        }
        try {
            String reason = rejectReason == null || rejectReason.isBlank() ? ""
                    : "?reason=" + RestClient.encode(rejectReason.trim());
            RestClient.post("consignment/reject/" + answering.getId() + reason, null, null, userId(), locale());
            info("cons.msg.rejected");
            refresh();
            PrimeFaces.current().executeScript("PF('consRejectDlg').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ store side

    public void openInvite() {
        searchText = null;
        chosen = null;
        commission = null;
        frequency = "BIWEEKLY";
        message = null;
        search();
    }

    public void search() {
        try {
            String q = searchText == null || searchText.isBlank() ? "" : "?q=" + RestClient.encode(searchText.trim());
            List<BusinessSummaryVo> list = RestClient.get("consignment/searchSuppliers" + q, SUPPLIER_LIST, userId(), locale());
            candidates = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            candidates = new ArrayList<>();
            errors(e);
        }
    }

    public void choose(BusinessSummaryVo b) {
        chosen = b;
    }

    public void clearChoice() {
        chosen = null;
    }

    public void sendInvite() {
        if (chosen == null) {
            errorKey("cons.invite.chooseFirst");
            return;
        }
        ConsignmentInviteVo vo = new ConsignmentInviteVo();
        vo.setSupplierBusinessId(chosen.getId());
        vo.setCommissionPercent(commission);
        vo.setPaymentFrequency(frequency);
        vo.setMessage(message);
        try {
            RestClient.post("consignment/invite", vo, null, userId(), locale());
            info("cons.msg.invited");
            refresh();
            PrimeFaces.current().executeScript("PF('consInviteDlg').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void cancelInvitation(ConsignmentAgreementVo a) {
        try {
            RestClient.post("consignment/cancelInvitation/" + a.getId(), null, null, userId(), locale());
            info("cons.msg.cancelled");
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ view helpers

    /** Share the supplier keeps per sale, for the live preview of the invite. */
    public BigDecimal getSupplierShare() {
        return commission == null ? null : HUNDRED.subtract(commission);
    }

    public String logoSrc(BusinessSummaryVo b) {
        return b != null && b.getLogoBase64() != null ? "data:image/png;base64," + b.getLogoBase64() : null;
    }

    public String initials(BusinessSummaryVo b) {
        if (b == null || b.getName() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String w : b.getName().trim().split("\\s+")) {
            if (!w.isEmpty() && Character.isLetterOrDigit(w.charAt(0))) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (sb.length() == 2) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    public String fmtDayIso(String yyyyMMdd) {
        return sessionMb.fmtDay(yyyyMMdd);
    }

    public String pct(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString() + " %";
    }

    public String frequencyLabel(String f) {
        return sessionMb.text("cons.freq." + (f != null ? f : "BIWEEKLY"));
    }

    public String statusLabel(String s) {
        return sessionMb.text("cons.status." + s);
    }

    public String statusClass(String s) {
        if ("ACTIVE".equals(s)) {
            return "badge badge-success";
        }
        if ("PENDING".equals(s)) {
            return "badge badge-warning";
        }
        if ("REJECTED".equals(s)) {
            return "badge badge-danger";
        }
        return "badge badge-muted";
    }

    public boolean isModuleOn() {
        return Boolean.TRUE.equals(summary.getModuleOn());
    }

    public boolean isEmpty() {
        return summary.getPendingInvitations().isEmpty() && summary.getStores().isEmpty()
                && summary.getSuppliers().isEmpty();
    }

    public ConsignmentSummaryVo getSummary() {
        return summary;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<BusinessSummaryVo> getCandidates() {
        return candidates;
    }

    public BusinessSummaryVo getChosen() {
        return chosen;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ConsignmentAgreementVo getAnswering() {
        return answering;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
