package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** Special commission of an employee for one service (EmployeeVo.serviceCommissions). */
public class ServiceCommissionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;

    private BigDecimal commissionPercent;

    public ServiceCommissionVo() {
    }

    public ServiceCommissionVo(Integer serviceId, BigDecimal commissionPercent) {
        this.serviceId = serviceId;
        this.commissionPercent = commissionPercent;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }
}
