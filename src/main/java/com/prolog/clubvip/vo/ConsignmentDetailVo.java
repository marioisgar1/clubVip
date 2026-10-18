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

    /** Piezas vendidas por la tienda */
    private int piecesSold;
    /** Lo cobrado por la tienda */
    private BigDecimal soldValue = BigDecimal.ZERO;
    /** Lo que se queda la tienda */
    private BigDecimal soldCommission = BigDecimal.ZERO;
    /** Lo que le toca al proveedor */
    private BigDecimal soldNet = BigDecimal.ZERO;
    /** Ya pagado */
    private BigDecimal paidNet = BigDecimal.ZERO;
    /** Pendiente de pago */
    private BigDecimal pendingNet = BigDecimal.ZERO;
    /** Renglones pendientes */
    private int pendingCount;

    /** Pagado por la tienda, falta que el proveedor lo cierre */
    private BigDecimal reportedNet = BigDecimal.ZERO;
    /** Renglones con pago reportado */
    private int reportedCount;
    /** Lado tienda: puede pagar lo pendiente */
    private boolean canPay;
    /** Lado proveedor: puede cerrar o rechazar pagos */
    private boolean canConfirm;
    /** Pagos reportados por la tienda */
    private List<ConsignmentPaymentVo> payments = new ArrayList<>();
    /** Lo vendido */
    private List<ConsignmentSaleLineVo> sales = new ArrayList<>();

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

    public int getPiecesSold() {
        return piecesSold;
    }

    public void setPiecesSold(int piecesSold) {
        this.piecesSold = piecesSold;
    }

    public BigDecimal getSoldValue() {
        return soldValue;
    }

    public void setSoldValue(BigDecimal soldValue) {
        this.soldValue = soldValue;
    }

    public BigDecimal getSoldCommission() {
        return soldCommission;
    }

    public void setSoldCommission(BigDecimal soldCommission) {
        this.soldCommission = soldCommission;
    }

    public BigDecimal getSoldNet() {
        return soldNet;
    }

    public void setSoldNet(BigDecimal soldNet) {
        this.soldNet = soldNet;
    }

    public BigDecimal getPaidNet() {
        return paidNet;
    }

    public void setPaidNet(BigDecimal paidNet) {
        this.paidNet = paidNet;
    }

    public BigDecimal getPendingNet() {
        return pendingNet;
    }

    public void setPendingNet(BigDecimal pendingNet) {
        this.pendingNet = pendingNet;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }


    public List<ConsignmentSaleLineVo> getSales() {
        return sales;
    }

    public void setSales(List<ConsignmentSaleLineVo> sales) {
        this.sales = sales;
    }

    public BigDecimal getReportedNet() {
        return reportedNet;
    }

    public void setReportedNet(BigDecimal reportedNet) {
        this.reportedNet = reportedNet;
    }

    public int getReportedCount() {
        return reportedCount;
    }

    public void setReportedCount(int reportedCount) {
        this.reportedCount = reportedCount;
    }

    public boolean isCanPay() {
        return canPay;
    }

    public void setCanPay(boolean canPay) {
        this.canPay = canPay;
    }

    public boolean isCanConfirm() {
        return canConfirm;
    }

    public void setCanConfirm(boolean canConfirm) {
        this.canConfirm = canConfirm;
    }

    public List<ConsignmentPaymentVo> getPayments() {
        return payments;
    }

    public void setPayments(List<ConsignmentPaymentVo> payments) {
        this.payments = payments;
    }
}
