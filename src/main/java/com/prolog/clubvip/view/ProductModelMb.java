package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.ProductModelVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Product models / styles of the business (MODEL_VIEW): productModel/getProductModels · productModel/saveUpdateProductModel · productModel/deleteProductModels/{id}
 */
@Named("productModelMb")
@ViewScoped
public class ProductModelMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<ProductModelVo>>() { }.getType();

    private List<ProductModelVo> models = new ArrayList<>();
    private List<ProductModelVo> filtered;
    private ProductModelVo selected = new ProductModelVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<ProductModelVo> list = RestClient.get("productModel/getProductModels", LIST_TYPE, userId(), locale());
            models = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            models = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new ProductModelVo();
    }

    public void prepareEdit(ProductModelVo vo) {
        selected = new ProductModelVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setDescription(vo.getDescription());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        try {
            RestClient.post("productModel/saveUpdateProductModel", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('productModelDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(ProductModelVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("productModel/deleteProductModels/" + vo.getId(), userId(), locale());
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

    public List<ProductModelVo> getModels() {
        return models;
    }

    public List<ProductModelVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<ProductModelVo> filtered) {
        this.filtered = filtered;
    }

    public ProductModelVo getSelected() {
        return selected;
    }

    public void setSelected(ProductModelVo selected) {
        this.selected = selected;
    }
}
