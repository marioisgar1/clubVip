package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.StockCountItemVo;
import com.prolog.clubvip.vo.StockCountVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Physical stock count (STOCK_COUNT): open a count, capture counted quantities item by item and close it
 * to adjust stock. stockCount/openCount · getCounts · getCount/{id} · saveCountItem/{id} · closeCount/{id}
 */
@Named("stockCountMb")
@ViewScoped
public class StockCountMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type COUNT_LIST = new TypeToken<List<StockCountVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<StockCountVo> counts = new ArrayList<>();
    private StockCountVo current;
    private String newNotes;

    @PostConstruct
    public void init() {
        refresh();
        // land on the open count, if any
        for (StockCountVo c : counts) {
            if (isOpen(c)) {
                loadCount(c.getId());
                break;
            }
        }
    }

    public void refresh() {
        try {
            List<StockCountVo> list = RestClient.get("stockCount/getCounts", COUNT_LIST, userId(), sessionMb.getLocale());
            counts = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            counts = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    public void prepareNew() {
        newNotes = null;
    }

    public boolean isAnyOpen() {
        for (StockCountVo c : counts) {
            if (isOpen(c)) {
                return true;
            }
        }
        return false;
    }

    public void openCount() {
        StockCountVo payload = new StockCountVo();
        payload.setNotes(newNotes);
        try {
            StockCountVo opened = RestClient.post("stockCount/openCount", payload, StockCountVo.class,
                    userId(), sessionMb.getLocale());
            info(sessionMb.text("cat.count.opened"));
            newNotes = null;
            refresh();
            if (opened != null && opened.getId() != null) {
                loadCount(opened.getId());
            }
            PrimeFaces.current().executeScript("PF('newCountDialog').hide()");
        } catch (RestException e) {
            errors(e.getMessages());
        }
    }

    public void select(StockCountVo count) {
        if (count != null && count.getId() != null) {
            loadCount(count.getId());
        }
    }

    public void loadCount(Integer id) {
        try {
            current = RestClient.get("stockCount/getCount/" + id, StockCountVo.class, userId(), sessionMb.getLocale());
        } catch (RestException e) {
            errors(e.getMessages());
        }
    }

    private void reloadCurrent() {
        if (current != null && current.getId() != null) {
            loadCount(current.getId());
        }
    }

    public void saveItem(StockCountItemVo item) {
        if (current == null || item == null) {
            return;
        }
        if (item.getCountedQuantity() == null || item.getCountedQuantity() < 0) {
            warn(sessionMb.text("cat.count.invalidCounted"));
            return;
        }
        RestResponse r = RestClient.post("stockCount/saveCountItem/" + current.getId()
                + "?productId=" + item.getProductId() + "&countedQuantity=" + item.getCountedQuantity(),
                null, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("cat.count.itemSaved"));
            reloadCurrent();
            refresh();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void closeCount() {
        if (current == null || current.getId() == null) {
            return;
        }
        RestResponse r = RestClient.post("stockCount/closeCount/" + current.getId(), null, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("cat.count.closed"));
            refresh();
            reloadCurrent();
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public boolean isOpen(StockCountVo count) {
        return count != null && "OPEN".equalsIgnoreCase(count.getCountStatus());
    }

    public boolean isCurrentOpen() {
        return isOpen(current);
    }

    /** Row class: shortage (red) / surplus (gold) / neutral. */
    public String rowClass(StockCountItemVo item) {
        if (item == null || item.getCountedQuantity() == null || item.getDifference() == null) {
            return "";
        }
        if (item.getDifference() < 0) {
            return "row-shortage";
        }
        if (item.getDifference() > 0) {
            return "row-surplus";
        }
        return "";
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

    public List<StockCountVo> getCounts() {
        return counts;
    }

    public StockCountVo getCurrent() {
        return current;
    }

    public String getNewNotes() {
        return newNotes;
    }

    public void setNewNotes(String newNotes) {
        this.newNotes = newNotes;
    }
}
