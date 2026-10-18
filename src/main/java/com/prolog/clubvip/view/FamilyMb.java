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

    /** A new category starts as the existing ones: Talla and Color on, 3 and 4 off. */
    public void prepareNew() {
        selected = new FamilyVo();
        selected.setAttribute1Label(sessionMb.text("pos.family.attr.default1"));
        selected.setAttribute1Enabled(Boolean.TRUE);
        selected.setAttribute2Label(sessionMb.text("pos.family.attr.default2"));
        selected.setAttribute2Enabled(Boolean.TRUE);
        selected.setAttribute3Enabled(Boolean.FALSE);
        selected.setAttribute4Enabled(Boolean.FALSE);
    }

    public void prepareEdit(FamilyVo vo) {
        selected = new FamilyVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setStatus(vo.getStatus());
        selected.setAttribute1Label(vo.getAttribute1Label());
        selected.setAttribute1Enabled(Boolean.TRUE.equals(vo.getAttribute1Enabled()));
        selected.setAttribute2Label(vo.getAttribute2Label());
        selected.setAttribute2Enabled(Boolean.TRUE.equals(vo.getAttribute2Enabled()));
        selected.setAttribute3Label(vo.getAttribute3Label());
        selected.setAttribute3Enabled(Boolean.TRUE.equals(vo.getAttribute3Enabled()));
        selected.setAttribute4Label(vo.getAttribute4Label());
        selected.setAttribute4Enabled(Boolean.TRUE.equals(vo.getAttribute4Enabled()));
    }

    /** "Talla · Color" of the characteristics a category uses, for the table. */
    public String attributesOf(FamilyVo f) {
        StringBuilder sb = new StringBuilder();
        String[] labels = {f.getAttribute1Label(), f.getAttribute2Label(), f.getAttribute3Label(), f.getAttribute4Label()};
        Boolean[] on = {f.getAttribute1Enabled(), f.getAttribute2Enabled(), f.getAttribute3Enabled(), f.getAttribute4Enabled()};
        for (int i = 0; i < 4; i++) {
            if (Boolean.TRUE.equals(on[i]) && labels[i] != null && !labels[i].trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(labels[i].trim());
            }
        }
        return sb.toString();
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
