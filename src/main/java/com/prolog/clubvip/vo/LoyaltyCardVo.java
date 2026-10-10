package com.prolog.clubvip.vo;

import java.io.Serializable;

/**
 * Loyalty card of a customer in a business program.
 */
public class LoyaltyCardVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer businessId;

    private String businessName;

    private Integer programId;

    private String programName;

    private Integer customerId;

    private String customerName;

    private Integer stamps;

    private Integer servicesRequired;

    private Integer points;

    private Integer rewardsAvailable;

    private Integer rewardsRedeemed;

    private Integer totalStamps;

    private String rewardDescription;

    private Integer progressPercent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Integer getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getStamps() {
        return stamps;
    }

    public void setStamps(Integer stamps) {
        this.stamps = stamps;
    }

    public Integer getServicesRequired() {
        return servicesRequired;
    }

    public void setServicesRequired(Integer servicesRequired) {
        this.servicesRequired = servicesRequired;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getRewardsAvailable() {
        return rewardsAvailable;
    }

    public void setRewardsAvailable(Integer rewardsAvailable) {
        this.rewardsAvailable = rewardsAvailable;
    }

    public Integer getRewardsRedeemed() {
        return rewardsRedeemed;
    }

    public void setRewardsRedeemed(Integer rewardsRedeemed) {
        this.rewardsRedeemed = rewardsRedeemed;
    }

    public Integer getTotalStamps() {
        return totalStamps;
    }

    public void setTotalStamps(Integer totalStamps) {
        this.totalStamps = totalStamps;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }
}
