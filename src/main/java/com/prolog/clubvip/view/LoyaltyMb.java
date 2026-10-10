package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessCustomerVo;
import com.prolog.clubvip.vo.CustomerBenefitVo;
import com.prolog.clubvip.vo.LoyaltyCardVo;
import com.prolog.clubvip.vo.LoyaltyProgramVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.ServiceVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Loyalty: reward card configuration, customer progress (with redemption) and granted benefits.
 */
@Named("loyaltyMb")
@ViewScoped
public class LoyaltyMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type CARD_LIST = new TypeToken<List<LoyaltyCardVo>>() { }.getType();
    private static final Type SERVICE_LIST = new TypeToken<List<ServiceVo>>() { }.getType();
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final Type CUSTOMER_LIST = new TypeToken<List<BusinessCustomerVo>>() { }.getType();
    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    private SessionMb sessionMb;

    private LoyaltyProgramVo program = new LoyaltyProgramVo();
    private List<LoyaltyCardVo> cards = new ArrayList<>();
    private List<LoyaltyCardVo> filteredCards;
    private List<ServiceVo> services = new ArrayList<>();
    private List<ProductVo> products = new ArrayList<>();
    private List<BusinessCustomerVo> customers = new ArrayList<>();
    private CustomerBenefitVo benefit = new CustomerBenefitVo();
    private LocalDate benefitValidUntil;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            LoyaltyProgramVo p = RestClient.get("loyalty/getProgram", LoyaltyProgramVo.class, userId(), locale());
            program = p != null ? p : newProgram();
            if (program.getServiceIds() == null) {
                program.setServiceIds(new ArrayList<>());
            }
        } catch (RestException e) {
            program = newProgram();
            errors(e);
        }
        try {
            services = nz(RestClient.get("service/getServices", SERVICE_LIST, userId(), locale()));
        } catch (RestException e) {
            errors(e);
        }
        try {
            products = nz(RestClient.get("product/getProducts", PRODUCT_LIST, userId(), locale()));
        } catch (RestException e) {
            errors(e);
        }
        loadCards();
    }

    private LoyaltyProgramVo newProgram() {
        LoyaltyProgramVo p = new LoyaltyProgramVo();
        p.setServicesRequired(10);
        p.setPointsPerService(1);
        p.setRewardType("SERVICE");
        p.setAppliesTo("ALL");
        p.setServiceIds(new ArrayList<>());
        return p;
    }

    public void loadCards() {
        if (!sessionMb.isAdmin() && !sessionMb.isOperator()) {
            return;
        }
        try {
            cards = nz(RestClient.get("loyalty/getCards", CARD_LIST, userId(), locale()));
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ program

    public void saveProgram() {
        if ("ALL".equals(program.getAppliesTo())) {
            program.setServiceIds(new ArrayList<>());
        }
        if (!"SERVICE".equals(program.getRewardType())) {
            program.setRewardServiceId(null);
        }
        if (!"PRODUCT".equals(program.getRewardType())) {
            program.setRewardProductId(null);
        }
        if (!"DISCOUNT".equals(program.getRewardType())) {
            program.setRewardDiscountPercent(null);
        }
        RestResponse r = RestClient.post("loyalty/saveUpdateProgram", program, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            refresh();
        } else {
            errors(r);
        }
    }

    // ------------------------------------------------------------------ cards

    public void redeem(LoyaltyCardVo card) {
        if (card == null || card.getId() == null) {
            return;
        }
        RestResponse r = RestClient.post("loyalty/redeem/" + card.getId(), null, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.loyalty.redeemed"));
            loadCards();
        } else {
            errors(r);
        }
    }

    /** "3 de 10" progress caption for a card. */
    public String progressLabel(LoyaltyCardVo card) {
        int stamps = card.getStamps() != null ? card.getStamps() : 0;
        int required = card.getServicesRequired() != null ? card.getServicesRequired()
                : (program.getServicesRequired() != null ? program.getServicesRequired() : 0);
        return stamps + " " + sessionMb.text("rel.loyalty.of") + " " + required;
    }

    public int progressPercent(LoyaltyCardVo card) {
        if (card.getProgressPercent() != null) {
            return Math.max(0, Math.min(100, card.getProgressPercent()));
        }
        int stamps = card.getStamps() != null ? card.getStamps() : 0;
        int required = card.getServicesRequired() != null && card.getServicesRequired() > 0
                ? card.getServicesRequired() : 1;
        return Math.max(0, Math.min(100, stamps * 100 / required));
    }

    // ------------------------------------------------------------------ benefits

    public void prepareBenefit() {
        benefit = new CustomerBenefitVo();
        benefit.setBenefitType("BIRTHDAY");
        benefitValidUntil = LocalDate.now().plusDays(30);
        if (customers.isEmpty()) {
            try {
                customers = nz(RestClient.get("customer/getCustomers", CUSTOMER_LIST, userId(), locale()));
            } catch (RestException e) {
                errors(e);
            }
        }
    }

    public void grantBenefit() {
        benefit.setValidUntil(benefitValidUntil != null ? benefitValidUntil.format(ISO_DAY) : null);
        RestResponse r = RestClient.post("loyalty/grantBenefit", benefit, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("rel.loyalty.benefitGranted"));
            PrimeFaces.current().executeScript("PF('benefitDialog').hide()");
        } else {
            errors(r);
        }
    }

    // ------------------------------------------------------------------ helpers

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private static <T> List<T> nz(List<T> list) {
        return list != null ? list : new ArrayList<>();
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

    public LoyaltyProgramVo getProgram() {
        return program;
    }

    public void setProgram(LoyaltyProgramVo program) {
        this.program = program;
    }

    public List<LoyaltyCardVo> getCards() {
        return cards;
    }

    public List<LoyaltyCardVo> getFilteredCards() {
        return filteredCards;
    }

    public void setFilteredCards(List<LoyaltyCardVo> filteredCards) {
        this.filteredCards = filteredCards;
    }

    public List<ServiceVo> getServices() {
        return services;
    }

    public List<ProductVo> getProducts() {
        return products;
    }

    public List<BusinessCustomerVo> getCustomers() {
        return customers;
    }

    public CustomerBenefitVo getBenefit() {
        return benefit;
    }

    public void setBenefit(CustomerBenefitVo benefit) {
        this.benefit = benefit;
    }

    public LocalDate getBenefitValidUntil() {
        return benefitValidUntil;
    }

    public void setBenefitValidUntil(LocalDate benefitValidUntil) {
        this.benefitValidUntil = benefitValidUntil;
    }
}
