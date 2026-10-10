package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Full customer file for a business: profile, history, reviews, loyalty card and benefits.
 */
public class CustomerFileVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private BusinessCustomerVo customer;

    private List<AppointmentVo> history = new ArrayList<>();

    private List<ReviewVo> reviews = new ArrayList<>();

    private LoyaltyCardVo card;

    private List<CustomerBenefitVo> benefits = new ArrayList<>();

    public BusinessCustomerVo getCustomer() {
        return customer;
    }

    public void setCustomer(BusinessCustomerVo customer) {
        this.customer = customer;
    }

    public List<AppointmentVo> getHistory() {
        return history;
    }

    public void setHistory(List<AppointmentVo> history) {
        this.history = history;
    }

    public List<ReviewVo> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewVo> reviews) {
        this.reviews = reviews;
    }

    public LoyaltyCardVo getCard() {
        return card;
    }

    public void setCard(LoyaltyCardVo card) {
        this.card = card;
    }

    public List<CustomerBenefitVo> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<CustomerBenefitVo> benefits) {
        this.benefits = benefits;
    }
}
