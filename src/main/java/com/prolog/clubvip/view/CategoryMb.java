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
import com.prolog.clubvip.vo.BusinessCategoryVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Club VIP back office: CRUD of business categories ("giros").
 */
@Named("categoryMb")
@ViewScoped
public class CategoryMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<BusinessCategoryVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<BusinessCategoryVo> categories = new ArrayList<>();
    private List<BusinessCategoryVo> filtered;
    private BusinessCategoryVo selected = new BusinessCategoryVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<BusinessCategoryVo> list = RestClient.get("category/getCategories", LIST_TYPE, userId(), locale());
            categories = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            categories = new ArrayList<>();
            errors(e);
        }
        filtered = null;
    }

    public void prepareNew() {
        selected = new BusinessCategoryVo();
        selected.setRequiresLicense(Boolean.FALSE);
        selected.setSortOrder(categories.size() + 1);
    }

    public void prepareEdit(BusinessCategoryVo vo) {
        selected = new BusinessCategoryVo();
        selected.setId(vo.getId());
        selected.setCode(vo.getCode());
        selected.setName(vo.getName());
        selected.setDescription(vo.getDescription());
        selected.setRequiresLicense(vo.getRequiresLicense() != null ? vo.getRequiresLicense() : Boolean.FALSE);
        selected.setIcon(vo.getIcon());
        selected.setSortOrder(vo.getSortOrder());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        RestResponse r = RestClient.post("category/saveUpdateCategory", selected, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('categoryDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public void delete(BusinessCategoryVo vo) {
        RestResponse r = RestClient.delete("category/deleteCategories/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            errors(r);
        }
    }

    // ------------------------------------------------------------------ helpers

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private java.util.Locale locale() {
        return sessionMb.getLocale();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            error(m);
        }
    }

    private void errors(RestResponse r) {
        for (String m : RestClient.extractErrors(r.getBody())) {
            error(m);
        }
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // ------------------------------------------------------------------ getters / setters

    public List<BusinessCategoryVo> getCategories() {
        return categories;
    }

    public List<BusinessCategoryVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<BusinessCategoryVo> filtered) {
        this.filtered = filtered;
    }

    public BusinessCategoryVo getSelected() {
        return selected;
    }

    public void setSelected(BusinessCategoryVo selected) {
        this.selected = selected;
    }
}
