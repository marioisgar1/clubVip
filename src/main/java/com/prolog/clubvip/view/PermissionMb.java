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
import com.prolog.clubvip.vo.PermissionVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Security: permissions / menu entries CRUD.
 */
@Named("permissionMb")
@ViewScoped
public class PermissionMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<PermissionVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<PermissionVo> permissions = new ArrayList<>();
    private List<PermissionVo> filtered;
    private PermissionVo selected = new PermissionVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<PermissionVo> list = RestClient.get("permission/getPermissions", LIST_TYPE, userId(), locale());
            permissions = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        filtered = null;
    }

    public void prepareNew() {
        selected = new PermissionVo();
        selected.setIsMenu(Boolean.TRUE);
        selected.setSortOrder(permissions.size() + 1);
    }

    public void prepareEdit(PermissionVo vo) {
        selected = new PermissionVo();
        selected.setId(vo.getId());
        selected.setCode(vo.getCode());
        selected.setName(vo.getName());
        selected.setDescription(vo.getDescription());
        selected.setMenuGroup(vo.getMenuGroup());
        selected.setOutcomeWeb(vo.getOutcomeWeb());
        selected.setOutcomeApp(vo.getOutcomeApp());
        selected.setIcon(vo.getIcon());
        selected.setSortOrder(vo.getSortOrder());
        selected.setIsMenu(vo.getIsMenu() != null ? vo.getIsMenu() : Boolean.FALSE);
        selected.setStatus(vo.getStatus());
    }

    public void save() {
        RestResponse r = RestClient.post("permission/saveUpdatePermission", selected, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('permissionDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public void delete(PermissionVo vo) {
        RestResponse r = RestClient.delete("permission/deletePermissions/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            errors(r);
        }
    }

    /** Resolves the permission name (an i18n key) in the session locale. */
    public String nameOf(PermissionVo p) {
        return p != null && p.getName() != null ? sessionMb.text(p.getName()) : "";
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

    public List<PermissionVo> getPermissions() {
        return permissions;
    }

    public List<PermissionVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<PermissionVo> filtered) {
        this.filtered = filtered;
    }

    public PermissionVo getSelected() {
        return selected;
    }

    public void setSelected(PermissionVo selected) {
        this.selected = selected;
    }
}
