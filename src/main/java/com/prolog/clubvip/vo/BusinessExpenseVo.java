package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A running expense of the business (supplies, power, internet, water, rent, purchases...).
 * {@code expenseDate} travels as {@code yyyy-MM-dd}; {@code category} is one of
 * SUPPLIES, ELECTRICITY, INTERNET, WATER, RENT, PURCHASES, OTHER.
 */
public class BusinessExpenseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String expenseDate;

    private String category;

    private String description;

    private BigDecimal amount;

    private Integer status;

    /** User who registered the expense. */
    private Integer createdBy;

    /** Full name of that user ("Registró" column). */
    private String createdByName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}
