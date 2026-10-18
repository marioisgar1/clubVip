package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import org.primefaces.PrimeFaces;

/**
 * Inventory entries (INVENTORY_ENTRY): build a purchase / reception line by line and post it,
 * plus the history of previous entries. Same flow as the app: scan (or type) the barcode, a dialog
 * asks quantity and cost, and Agregar adds the line (Guardar entrada posts it).
 * inventory/saveEntry · inventory/getEntries · product/getProductByBarcode
 */
@Named("inventoryEntryMb")
@ViewScoped
public class InventoryEntryMb implements Serializable {

    private static final long serialVersionUID = 1L;
    /** Leonisa's acquisition-cost rule: suggested cost when there is no moving average yet. */
    private static final int FALLBACK_COST_PERCENT = 40;
    private static final Type ENTRY_LIST = new TypeToken<List<InventoryEntryVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<InventoryEntryVo> entries = new ArrayList<>();

    private String notes;
    private List<InventoryEntryItemVo> items = new ArrayList<>();
    private String barcode;
    /** Product found by the last scan: the one the line dialog is editing. */
    private ProductVo product;
    private Integer quantity = 1;
    private BigDecimal unitCost;

    @PostConstruct
    public void init() {
        refresh();
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

    /**
     * GET product/getProductByBarcode/{barcode} (USB readers type the code and press Enter) and open
     * the line dialog with quantity 1 and the average acquisition cost, or
     * {@link #FALLBACK_COST_PERCENT}% of the retail price when the product has none yet.
     */
    public void scan() {
        if (barcode == null || barcode.isBlank()) {
            return;
        }
        String code = barcode.trim();
        barcode = null;
        try {
            ProductVo p = RestClient.get("product/getProductByBarcode/" + RestClient.encode(code) + "?includeImages=false",
                    ProductVo.class, userId(), sessionMb.getLocale());
            if (p == null || p.getId() == null) {
                warn(sessionMb.text("pos.sale.notFound"));
                return;
            }
            product = p;
            quantity = 1;
            unitCost = isAverageCostKnown() ? p.getAverageCost() : getSuggestedCost();
            PrimeFaces.current().executeScript("PF('lineDialog').show()");
        } catch (RestException e) {
            errors(e.getMessages());
        }
    }

    /** True when the scanned product already has a moving-average acquisition cost. */
    public boolean isAverageCostKnown() {
        return product != null && product.getAverageCost() != null && product.getAverageCost().signum() > 0;
    }

    /** {@link #FALLBACK_COST_PERCENT}% of the retail price of the scanned product. */
    public BigDecimal getSuggestedCost() {
        BigDecimal retail = product != null && product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        return retail.multiply(BigDecimal.valueOf(FALLBACK_COST_PERCENT)).divide(BigDecimal.valueOf(100), 2,
                RoundingMode.HALF_UP);
    }

    public int getFallbackCostPercent() {
        return FALLBACK_COST_PERCENT;
    }

    public void cancelLine() {
        resetLine();
    }

    public void addItem() {
        ProductVo p = product;
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
                closeLine();
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
        closeLine();
    }

    private void closeLine() {
        resetLine();
        PrimeFaces.current().executeScript("PF('lineDialog').hide()");
    }

    private void resetLine() {
        product = null;
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
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ProductVo getProduct() {
        return product;
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
