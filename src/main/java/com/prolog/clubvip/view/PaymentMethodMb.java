package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.PaymentMethodVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Payment methods: name, card commission and the credit switch (PAYMENT_VIEW): paymentMethod/getPaymentMethods · paymentMethod/saveUpdatePaymentMethod · paymentMethod/deletePaymentMethods/{id}
 */
@Named("paymentMethodMb")
@ViewScoped
public class PaymentMethodMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<PaymentMethodVo>>() { }.getType();

    private List<PaymentMethodVo> paymentMethods = new ArrayList<>();
    private List<PaymentMethodVo> filtered;
    private PaymentMethodVo selected = new PaymentMethodVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<PaymentMethodVo> list = RestClient.get("paymentMethod/getPaymentMethods", LIST_TYPE, userId(), locale());
            paymentMethods = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            paymentMethods = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new PaymentMethodVo();
        selected.setCredit(Boolean.FALSE);
        selected.setCommissionPercent(java.math.BigDecimal.ZERO);
    }

    public void prepareEdit(PaymentMethodVo vo) {
        selected = new PaymentMethodVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setCommissionPercent(vo.getCommissionPercent());
        // Carried over on purpose: the switch always posts a value, so a null here would
        // silently turn the credit method into a plain one on the next save.
        selected.setCredit(Boolean.TRUE.equals(vo.getCredit()));
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        try {
            RestClient.post("paymentMethod/saveUpdatePaymentMethod", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('paymentMethodDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(PaymentMethodVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("paymentMethod/deletePaymentMethods/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            refresh();
            info("msg.deleted");
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public List<PaymentMethodVo> getPaymentMethods() {
        return paymentMethods;
    }

    public List<PaymentMethodVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<PaymentMethodVo> filtered) {
        this.filtered = filtered;
    }

    public PaymentMethodVo getSelected() {
        return selected;
    }

    public void setSelected(PaymentMethodVo selected) {
        this.selected = selected;
    }
}
