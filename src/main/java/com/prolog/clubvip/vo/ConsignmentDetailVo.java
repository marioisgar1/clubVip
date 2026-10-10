package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Consignment relation file: terms, the big numbers, pieces at the store and the shipments.
 */
public class ConsignmentDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private ConsignmentAgreementVo agreement;

    private Integer piecesAtStore;

    private BigDecimal valueAtStore;

    private Integer piecesInTransit;

    private List<ConsignmentStockVo> inventory = new ArrayList<>();

    private List<ConsignmentShipmentVo> shipments = new ArrayList<>();

    public ConsignmentAgreementVo getAgreement() {
        return agreement;
    }

    public void setAgreement(ConsignmentAgreementVo agreement) {
        this.agreement = agreement;
    }

    public Integer getPiecesAtStore() {
        return piecesAtStore;
    }

    public void setPiecesAtStore(Integer piecesAtStore) {
        this.piecesAtStore = piecesAtStore;
    }

    public BigDecimal getValueAtStore() {
        return valueAtStore;
    }

    public void setValueAtStore(BigDecimal valueAtStore) {
        this.valueAtStore = valueAtStore;
    }

    public Integer getPiecesInTransit() {
        return piecesInTransit;
    }

    public void setPiecesInTransit(Integer piecesInTransit) {
        this.piecesInTransit = piecesInTransit;
    }

    public List<ConsignmentStockVo> getInventory() {
        return inventory;
    }

    public void setInventory(List<ConsignmentStockVo> inventory) {
        this.inventory = inventory;
    }

    public List<ConsignmentShipmentVo> getShipments() {
        return shipments;
    }

    public void setShipments(List<ConsignmentShipmentVo> shipments) {
        this.shipments = shipments;
    }
}
