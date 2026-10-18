package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.SelectEvent;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.InventoryMovementVo;
import com.prolog.clubvip.vo.InventorySummaryVo;
import com.prolog.clubvip.vo.StockItemVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Stock on hand (INVENTORY_VIEW): KPI summary (admin only), stock table and the movements of the selected product.
 * inventory/getSummary · inventory/getStock · inventory/getMovements?productId=
 */
@Named("inventoryMb")
@ViewScoped
public class InventoryMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type STOCK_LIST = new TypeToken<List<StockItemVo>>() { }.getType();
    private static final Type MOVEMENT_LIST = new TypeToken<List<InventoryMovementVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private InventorySummaryVo summary;
    private boolean summaryAvailable;
    private List<StockItemVo> items = new ArrayList<>();
    private List<StockItemVo> filtered;
    private StockItemVo selected;
    private List<InventoryMovementVo> movements = new ArrayList<>();
    private boolean movementsAvailable = true;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        loadSummary();
        try {
            List<StockItemVo> list = RestClient.get("inventory/getStock", STOCK_LIST, userId(), sessionMb.getLocale());
            items = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            items = new ArrayList<>();
            errors(e.getMessages());
        }
        if (selected != null) {
            loadMovements();
        }
    }

    /** The summary is admin-only: a 403 for employees simply hides the KPI cards. */
    private void loadSummary() {
        try {
            summary = RestClient.get("inventory/getSummary", InventorySummaryVo.class, userId(), sessionMb.getLocale());
            summaryAvailable = summary != null;
        } catch (RestException e) {
            summary = null;
            summaryAvailable = false;
        }
    }

    public void onRowSelect(SelectEvent<StockItemVo> event) {
        selected = event.getObject();
        loadMovements();
    }

    public void onRowUnselect() {
        selected = null;
        movements = new ArrayList<>();
    }

    private void loadMovements() {
        movements = new ArrayList<>();
        if (selected == null || selected.getProductId() == null) {
            return;
        }
        try {
            List<InventoryMovementVo> list = RestClient.get("inventory/getMovements?productId=" + selected.getProductId(),
                    MOVEMENT_LIST, userId(), sessionMb.getLocale());
            movements = list != null ? list : new ArrayList<>();
            movementsAvailable = true;
        } catch (RestException e) {
            // employees are not allowed to read movements: keep the table empty, no error noise
            movementsAvailable = false;
        }
    }

    public String belowMinimumNames() {
        if (summary == null || summary.getBelowMinimumNames() == null || summary.getBelowMinimumNames().isEmpty()) {
            return "";
        }
        List<String> names = summary.getBelowMinimumNames();
        String joined = String.join(", ", names.subList(0, Math.min(3, names.size())));
        if (names.size() > 3) {
            joined += " +" + (names.size() - 3);
        }
        return joined;
    }

    /** CSS badge class for a movement type. */
    public String movementBadge(String type) {
        if (type == null) {
            return "badge badge-muted";
        }
        switch (type) {
            case "IN":
            case "RETURN":
            case "CONS_IN":
            case "CONS_DIFF":
            case "CONS_CANCEL":
            case "CONS_REJECT":
                return "badge badge-success";
            case "CONS_OUT":
                return "badge badge-info";
            case "SALE":
                return "badge badge-info";
            case "ADJUST":
                return "badge badge-warning";
            default:
                return "badge badge-muted";
        }
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void errors(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return;
        }
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, sessionMb.text("msg.error"), null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------------ getters / setters

    /** What the stock would earn sold at its price: value at sale price − value at average cost. */
    public java.math.BigDecimal getPotentialProfit() {
        if (summary == null) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal retail = summary.getRetailValue() != null ? summary.getRetailValue() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal cost = summary.getStockValue() != null ? summary.getStockValue() : java.math.BigDecimal.ZERO;
        return retail.subtract(cost);
    }

    public InventorySummaryVo getSummary() {
        return summary;
    }

    public boolean isSummaryAvailable() {
        return summaryAvailable;
    }

    public List<StockItemVo> getItems() {
        return items;
    }

    public List<StockItemVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<StockItemVo> filtered) {
        this.filtered = filtered;
    }

    public StockItemVo getSelected() {
        return selected;
    }

    public void setSelected(StockItemVo selected) {
        this.selected = selected;
    }

    public List<InventoryMovementVo> getMovements() {
        return movements;
    }

    public boolean isMovementsAvailable() {
        return movementsAvailable;
    }
}
