package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.InventoryEntryItemVo;
import com.prolog.clubvip.vo.InventoryEntryVo;
import com.prolog.clubvip.vo.ProductVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Inventory entries (INVENTORY_ENTRY): build a purchase / reception line by line and post it,
 * plus the history of previous entries. inventory/saveEntry · inventory/getEntries · product/getProducts
 */
@Named("inventoryEntryMb")
@ViewScoped
public class InventoryEntryMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final Type ENTRY_LIST = new TypeToken<List<InventoryEntryVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<ProductVo> products = new ArrayList<>();
    private List<InventoryEntryVo> entries = new ArrayList<>();

    private String notes;
    private List<InventoryEntryItemVo> items = new ArrayList<>();
    private Integer productId;
    private Integer quantity = 1;
    private BigDecimal unitCost;

    @PostConstruct
    public void init() {
        loadProducts();
        refresh();
    }

    private void loadProducts() {
        try {
            List<ProductVo> list = RestClient.get("product/getProducts?includeImages=false", PRODUCT_LIST,
                    userId(), sessionMb.getLocale());
            products = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            products = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    public void refresh() {
        try {
            List<InventoryEntryVo> list = RestClient.get("inventory/getEntries", ENTRY_LIST, userId(), sessionMb.getLocale());
            entries = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            entries = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    /** When a product is picked, propose its current average cost as unit cost. */
    public void onProductChange() {
        ProductVo p = findProduct(productId);
        if (p != null && p.getAverageCost() != null && p.getAverageCost().signum() > 0) {
            unitCost = p.getAverageCost();
        }
    }

    public void addItem() {
        ProductVo p = findProduct(productId);
        if (p == null) {
            warn(sessionMb.text("cat.entry.selectProduct"));
            return;
        }
        if (quantity == null || quantity <= 0) {
            warn(sessionMb.text("cat.entry.invalidQuantity"));
            return;
        }
        if (unitCost == null || unitCost.signum() < 0) {
            warn(sessionMb.text("cat.entry.invalidCost"));
            return;
        }
        // merge with an existing line of the same product and cost
        for (InventoryEntryItemVo it : items) {
            if (p.getId().equals(it.getProductId()) && unitCost.compareTo(it.getUnitCost()) == 0) {
                it.setQuantity(it.getQuantity() + quantity);
                it.setLineTotal(unitCost.multiply(BigDecimal.valueOf(it.getQuantity())));
                resetLine();
                return;
            }
        }
        InventoryEntryItemVo it = new InventoryEntryItemVo();
        it.setProductId(p.getId());
        it.setProductName(p.getName());
        it.setQuantity(quantity);
        it.setUnitCost(unitCost);
        it.setLineTotal(unitCost.multiply(BigDecimal.valueOf(quantity)));
        items.add(it);
        resetLine();
    }

    private void resetLine() {
        productId = null;
        quantity = 1;
        unitCost = null;
    }

    public void removeItem(InventoryEntryItemVo item) {
        items.remove(item);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (InventoryEntryItemVo it : items) {
            if (it.getLineTotal() != null) {
                total = total.add(it.getLineTotal());
            }
        }
        return total;
    }

    public void save() {
        if (items.isEmpty()) {
            warn(sessionMb.text("cat.entry.noItems"));
            return;
        }
        InventoryEntryVo payload = new InventoryEntryVo();
        payload.setNotes(notes);
        payload.setItems(items);
        RestResponse r = RestClient.post("inventory/saveEntry", payload, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("cat.entry.saved"));
            items = new ArrayList<>();
            notes = null;
            resetLine();
            loadProducts();
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    private ProductVo findProduct(Integer id) {
        if (id == null) {
            return null;
        }
        for (ProductVo p : products) {
            if (id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void warn(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
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

    public List<ProductVo> getProducts() {
        return products;
    }

    public List<InventoryEntryVo> getEntries() {
        return entries;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<InventoryEntryItemVo> getItems() {
        return items;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
