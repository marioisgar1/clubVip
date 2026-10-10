package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.SaleLocationVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Sale locations (tianguis, bazaars, malls, marketplaces) (LOCATION_VIEW): saleLocation/getSaleLocations · saleLocation/saveUpdateSaleLocation · saleLocation/deleteSaleLocations/{id}
 */
@Named("saleLocationMb")
@ViewScoped
public class SaleLocationMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<SaleLocationVo>>() { }.getType();
    private static final String[] LOCATION_TYPES = { "SHOP", "TIANGUIS", "BAZAR", "MALL", "MARKETPLACE", "OTHER" };

    private List<SaleLocationVo> saleLocations = new ArrayList<>();
    private List<SaleLocationVo> filtered;
    private SaleLocationVo selected = new SaleLocationVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<SaleLocationVo> list = RestClient.get("saleLocation/getSaleLocations", LIST_TYPE, userId(), locale());
            saleLocations = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            saleLocations = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new SaleLocationVo();
    }

    public void prepareEdit(SaleLocationVo vo) {
        selected = new SaleLocationVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setLocationType(vo.getLocationType());
        selected.setAddress(vo.getAddress());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        try {
            RestClient.post("saleLocation/saveUpdateSaleLocation", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('saleLocationDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(SaleLocationVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("saleLocation/deleteSaleLocations/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            refresh();
            info("msg.deleted");
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    public String[] getLocationTypes() {
        return LOCATION_TYPES;
    }

    /** Translated label of a location type ({@code pos.locationType.TIANGUIS} ...), the raw code when unknown. */
    public String typeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        String key = "pos.locationType." + type;
        String text = sessionMb.text(key);
        return key.equals(text) ? type : text;
    }

    // ------------------------------------------------------------------ getters / setters

    public List<SaleLocationVo> getSaleLocations() {
        return saleLocations;
    }

    public List<SaleLocationVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<SaleLocationVo> filtered) {
        this.filtered = filtered;
    }

    public SaleLocationVo getSelected() {
        return selected;
    }

    public void setSelected(SaleLocationVo selected) {
        this.selected = selected;
    }
}
