package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Outcome of an exchange, ready to be printed on the ticket:
 * {@code difference = newAmount - creditAmount}. When it is positive the customer pays it;
 * when it is negative nothing is refunded and the surplus is reported as
 * {@code forfeitedCredit} so the operator can tell the customer before confirming.
 */
public class SaleExchangeResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String folio;

    private Integer originSaleId;
    private String originFolio;

    /** Value credited for the goods brought back (price actually charged back then). */
    private BigDecimal creditAmount;

    /** Value of the goods the customer takes, at today's price. */
    private BigDecimal newAmount;

    /** Positive: the customer pays. Zero: clean exchange. Negative: credit left over. */
    private BigDecimal difference;

    /** Credit not used and not refunded (only when the difference is negative). */
    private BigDecimal forfeitedCredit;

    /** What the customer actually pays now: {@code max(difference, 0)}. */
    private BigDecimal amountToPay;

    private SaleVo sale;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Integer getOriginSaleId() {
        return originSaleId;
    }

    public void setOriginSaleId(Integer originSaleId) {
        this.originSaleId = originSaleId;
    }

    public String getOriginFolio() {
        return originFolio;
    }

    public void setOriginFolio(String originFolio) {
        this.originFolio = originFolio;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(BigDecimal newAmount) {
        this.newAmount = newAmount;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public BigDecimal getForfeitedCredit() {
        return forfeitedCredit;
    }

    public void setForfeitedCredit(BigDecimal forfeitedCredit) {
        this.forfeitedCredit = forfeitedCredit;
    }

    public BigDecimal getAmountToPay() {
        return amountToPay;
    }

    public void setAmountToPay(BigDecimal amountToPay) {
        this.amountToPay = amountToPay;
    }

    public SaleVo getSale() {
        return sale;
    }

    public void setSale(SaleVo sale) {
        this.sale = sale;
    }
}
