package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BrandVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Product brands of the business (BRAND_VIEW): brand/getBrands · brand/saveUpdateBrand · brand/deleteBrands/{id}
 */
@Named("brandMb")
@ViewScoped
public class BrandMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<BrandVo>>() { }.getType();

    private List<BrandVo> brands = new ArrayList<>();
    private List<BrandVo> filtered;
    private BrandVo selected = new BrandVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<BrandVo> list = RestClient.get("brand/getBrands", LIST_TYPE, userId(), locale());
            brands = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            brands = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new BrandVo();
    }

    public void prepareEdit(BrandVo vo) {
        selected = new BrandVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        try {
            RestClient.post("brand/saveUpdateBrand", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('brandDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(BrandVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("brand/deleteBrands/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            refresh();
            info("msg.deleted");
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public List<BrandVo> getBrands() {
        return brands;
    }

    public List<BrandVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<BrandVo> filtered) {
        this.filtered = filtered;
    }

    public BrandVo getSelected() {
        return selected;
    }

    public void setSelected(BrandVo selected) {
        this.selected = selected;
    }
}
