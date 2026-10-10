package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Available time slots of a business for a service on a given day.
 */
public class AvailabilityVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private Integer businessId;

    private Integer serviceId;

    private Integer durationMinutes;

    private List<TimeSlotVo> slots = new ArrayList<>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<TimeSlotVo> getSlots() {
        return slots;
    }

    public void setSlots(List<TimeSlotVo> slots) {
        this.slots = slots;
    }
}
