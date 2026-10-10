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
import com.prolog.clubvip.vo.ProfileVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Security: profiles and their permission set.
 */
@Named("profileMb")
@ViewScoped
public class ProfileMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<ProfileVo>>() { }.getType();
    private static final Type PERMISSION_LIST_TYPE = new TypeToken<List<PermissionVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<ProfileVo> profiles = new ArrayList<>();
    private List<ProfileVo> filtered;
    private List<PermissionVo> allPermissions = new ArrayList<>();
    private ProfileVo selected = new ProfileVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<ProfileVo> list = RestClient.get("profile/getProfiles", LIST_TYPE, userId(), locale());
            profiles = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        try {
            List<PermissionVo> list = RestClient.get("permission/getPermissions", PERMISSION_LIST_TYPE, userId(), locale());
            allPermissions = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        filtered = null;
    }

    public void prepareNew() {
        selected = new ProfileVo();
        selected.setPermissionIds(new ArrayList<>());
    }

    public void prepareEdit(ProfileVo vo) {
        selected = new ProfileVo();
        selected.setId(vo.getId());
        selected.setName(vo.getName());
        selected.setDescription(vo.getDescription());
        selected.setStatus(vo.getStatus());
        selected.setPermissionIds(vo.getPermissionIds() != null ? new ArrayList<>(vo.getPermissionIds()) : new ArrayList<>());
    }

    public void save() {
        RestResponse r = RestClient.post("profile/saveUpdateProfile", selected, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('profileDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public void delete(ProfileVo vo) {
        RestResponse r = RestClient.delete("profile/deleteProfiles/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            errors(r);
        }
    }

    /** Label of a permission for the checkbox list: i18n name + group. */
    public String permissionLabel(PermissionVo p) {
        String name = sessionMb.text(p.getName());
        return name + (p.getMenuGroup() != null ? " (" + p.getMenuGroup() + ")" : "");
    }

    public int permissionCount(ProfileVo p) {
        return p != null && p.getPermissionIds() != null ? p.getPermissionIds().size() : 0;
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

    public List<ProfileVo> getProfiles() {
        return profiles;
    }

    public List<ProfileVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<ProfileVo> filtered) {
        this.filtered = filtered;
    }

    public List<PermissionVo> getAllPermissions() {
        return allPermissions;
    }

    public ProfileVo getSelected() {
        return selected;
    }

    public void setSelected(ProfileVo selected) {
        this.selected = selected;
    }
}
