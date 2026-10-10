package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Consignment shipment: TO_STORE (supplier sends) or TO_SUPPLIER (store returns), seen from my business.
 */
public class ConsignmentShipmentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer agreementId;

    private String direction;

    private String folio;

    private String shipmentStatus;

    private Integer totalPieces;

    private Integer receivedPieces;

    private BigDecimal totalValue;

    private String notes;

    private String receiveComment;

    private String sentAt;

    private String receivedAt;

    private String partnerName;

    private Boolean canReceive;

    private Boolean canCancel;

    private List<ConsignmentShipmentItemVo> items = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(Integer agreementId) {
        this.agreementId = agreementId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public Integer getTotalPieces() {
        return totalPieces;
    }

    public void setTotalPieces(Integer totalPieces) {
        this.totalPieces = totalPieces;
    }

    public Integer getReceivedPieces() {
        return receivedPieces;
    }

    public void setReceivedPieces(Integer receivedPieces) {
        this.receivedPieces = receivedPieces;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReceiveComment() {
        return receiveComment;
    }

    public void setReceiveComment(String receiveComment) {
        this.receiveComment = receiveComment;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public Boolean getCanReceive() {
        return canReceive;
    }

    public void setCanReceive(Boolean canReceive) {
        this.canReceive = canReceive;
    }

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public List<ConsignmentShipmentItemVo> getItems() {
        return items;
    }

    public void setItems(List<ConsignmentShipmentItemVo> items) {
        this.items = items;
    }
}
