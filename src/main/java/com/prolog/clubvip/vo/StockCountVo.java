package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Physical stock count with its items and progress.
 */
public class StockCountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Date countDate;

    private String countStatus;

    private Integer userId;

    private String notes;

    private Integer totalItems;

    private Integer countedItems;

    private Integer pendingItems;

    private BigDecimal differenceValue;

    private List<StockCountItemVo> items = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCountDate() {
        return countDate;
    }

    public void setCountDate(Date countDate) {
        this.countDate = countDate;
    }

    public String getCountStatus() {
        return countStatus;
    }

    public void setCountStatus(String countStatus) {
        this.countStatus = countStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getCountedItems() {
        return countedItems;
    }

    public void setCountedItems(Integer countedItems) {
        this.countedItems = countedItems;
    }

    public Integer getPendingItems() {
        return pendingItems;
    }

    public void setPendingItems(Integer pendingItems) {
        this.pendingItems = pendingItems;
    }

    public BigDecimal getDifferenceValue() {
        return differenceValue;
    }

    public void setDifferenceValue(BigDecimal differenceValue) {
        this.differenceValue = differenceValue;
    }

    public List<StockCountItemVo> getItems() {
        return items;
    }

    public void setItems(List<StockCountItemVo> items) {
        this.items = items;
    }
}
