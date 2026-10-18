package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Subscription status update of a business.
 */
public class SubscriptionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer businessId;

    private String subscriptionStatus;

    private String subscriptionUntil;
    /** COURTESY | MONTHLY | ANNUAL; empty string clears it, null leaves it as is. */
    private String membershipPlan;

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionUntil() {
        return subscriptionUntil;
    }

    public void setSubscriptionUntil(String subscriptionUntil) {
        this.subscriptionUntil = subscriptionUntil;
    }

    public String getMembershipPlan() {
        return membershipPlan;
    }

    public void setMembershipPlan(String membershipPlan) {
        this.membershipPlan = membershipPlan;
    }
}
