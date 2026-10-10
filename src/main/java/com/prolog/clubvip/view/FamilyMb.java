package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.FamilyVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Product families of the business (FAMILY_VIEW): family/getFamilies · family/saveUpdateFamily · family/deleteFamilies/{id}
 */
@Named("familyMb")
@ViewScoped
public class FamilyMb extends BaseMb implements Refreshable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<FamilyVo>>() { }.getType();

    private List<FamilyVo> families = new ArrayList<>();
    private List<FamilyVo> filtered;
    private FamilyVo selected = new FamilyVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            List<FamilyVo> list = RestClient.get("family/getFamilies", LIST_TYPE, userId(), locale());
            families = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            families = new ArrayList<>();
            errors(e);
        }
    }

    public void prepareNew() {
        selected = new FamilyVo();
    }

    public void prepareEdit(FamilyVo vo) {
        selected = new FamilyVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        try {
            RestClient.post("family/saveUpdateFamily", selected, null, userId(), locale());
            refresh();
            info("msg.saved");
            PrimeFaces.current().executeScript("PF('familyDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(FamilyVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("family/deleteFamilies/" + vo.getId(), userId(), locale());
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

    public List<FamilyVo> getFamilies() {
        return families;
    }

    public List<FamilyVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<FamilyVo> filtered) {
        this.filtered = filtered;
    }

    public FamilyVo getSelected() {
        return selected;
    }

    public void setSelected(FamilyVo selected) {
        this.selected = selected;
    }
}
