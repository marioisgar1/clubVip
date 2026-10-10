package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Purchase / stock entry with its lines.
 */
public class InventoryEntryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Date entryDate;

    private Integer userId;

    private String notes;

    private BigDecimal totalCost;

    private List<InventoryEntryItemVo> items = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public List<InventoryEntryItemVo> getItems() {
        return items;
    }

    public void setItems(List<InventoryEntryItemVo> items) {
        this.items = items;
    }
}
