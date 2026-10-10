package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Consignment hub: invitations waiting for the caller's answer, stores that sell the caller's
 * products (caller = supplier) and suppliers whose products the caller sells (caller = store).
 */
public class ConsignmentSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean moduleOn;

    private List<ConsignmentAgreementVo> pendingInvitations = new ArrayList<>();

    private List<ConsignmentAgreementVo> stores = new ArrayList<>();

    private List<ConsignmentAgreementVo> suppliers = new ArrayList<>();

    /** Shipments in transit that my business has to receive. */
    private List<ConsignmentShipmentVo> pendingShipments = new ArrayList<>();

    public Boolean getModuleOn() {
        return moduleOn;
    }

    public void setModuleOn(Boolean moduleOn) {
        this.moduleOn = moduleOn;
    }

    public List<ConsignmentAgreementVo> getPendingInvitations() {
        return pendingInvitations;
    }

    public void setPendingInvitations(List<ConsignmentAgreementVo> pendingInvitations) {
        this.pendingInvitations = pendingInvitations;
    }

    public List<ConsignmentAgreementVo> getStores() {
        return stores;
    }

    public void setStores(List<ConsignmentAgreementVo> stores) {
        this.stores = stores;
    }

    public List<ConsignmentAgreementVo> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<ConsignmentAgreementVo> suppliers) {
        this.suppliers = suppliers;
    }

    public List<ConsignmentShipmentVo> getPendingShipments() {
        return pendingShipments;
    }

    public void setPendingShipments(List<ConsignmentShipmentVo> pendingShipments) {
        this.pendingShipments = pendingShipments;
    }
}
