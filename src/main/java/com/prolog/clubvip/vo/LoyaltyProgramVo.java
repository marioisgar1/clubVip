package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Loyalty program configuration of a business.
 */
public class LoyaltyProgramVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer businessId;

    private String name;

    private Integer servicesRequired;

    private Integer pointsPerService;

    private String rewardType;

    private String rewardDescription;

    private Integer rewardServiceId;

    private Integer rewardProductId;

    private BigDecimal rewardDiscountPercent;

    private String appliesTo;

    private List<Integer> serviceIds = new ArrayList<>();

    private Integer status;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getServicesRequired() {
        return servicesRequired;
    }

    public void setServicesRequired(Integer servicesRequired) {
        this.servicesRequired = servicesRequired;
    }

    public Integer getPointsPerService() {
        return pointsPerService;
    }

    public void setPointsPerService(Integer pointsPerService) {
        this.pointsPerService = pointsPerService;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public Integer getRewardServiceId() {
        return rewardServiceId;
    }

    public void setRewardServiceId(Integer rewardServiceId) {
        this.rewardServiceId = rewardServiceId;
    }

    public Integer getRewardProductId() {
        return rewardProductId;
    }

    public void setRewardProductId(Integer rewardProductId) {
        this.rewardProductId = rewardProductId;
    }

    public BigDecimal getRewardDiscountPercent() {
        return rewardDiscountPercent;
    }

    public void setRewardDiscountPercent(BigDecimal rewardDiscountPercent) {
        this.rewardDiscountPercent = rewardDiscountPercent;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
