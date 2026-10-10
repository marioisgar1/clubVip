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
import com.prolog.clubvip.vo.ProfileVo;
import com.prolog.clubvip.vo.UserVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Club VIP back office: every user of the platform (all businesses), with profile and business assignment.
 */
@Named("operatorUserMb")
@ViewScoped
public class OperatorUserMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<UserVo>>() { }.getType();
    private static final Type PROFILE_LIST_TYPE = new TypeToken<List<ProfileVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<UserVo> users = new ArrayList<>();
    private List<UserVo> filtered;
    private List<ProfileVo> allProfiles = new ArrayList<>();
    private UserVo selected = new UserVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<UserVo> list = RestClient.get("user/getUsers", LIST_TYPE, userId(), locale());
            users = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        try {
            List<ProfileVo> list = RestClient.get("profile/getProfiles", PROFILE_LIST_TYPE, userId(), locale());
            allProfiles = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        filtered = null;
    }

    public void prepareNew() {
        selected = new UserVo();
        selected.setPreferredLanguage("es");
        selected.setPreferredTheme("light");
    }

    public void prepareEdit(UserVo vo) {
        selected = new UserVo();
        selected.setId(vo.getId());
        selected.setEmail(vo.getEmail());
        selected.setPhone(vo.getPhone());
        selected.setFullName(vo.getFullName());
        selected.setProfileId(vo.getProfileId());
        selected.setProfileName(vo.getProfileName());
        selected.setBusinessId(vo.getBusinessId());
        selected.setBusinessName(vo.getBusinessName());
        selected.setPreferredLanguage(vo.getPreferredLanguage() != null ? vo.getPreferredLanguage() : "es");
        selected.setPreferredTheme(vo.getPreferredTheme() != null ? vo.getPreferredTheme() : "light");
        selected.setStatus(vo.getStatus());
        selected.setPassword(null); // blank on edit = keep current
    }

    public void save() {
        RestResponse r = RestClient.post("user/saveUpdateUser", selected, userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('userDialog').hide()");
            refresh();
        } else {
            errors(r);
        }
    }

    public void delete(UserVo vo) {
        RestResponse r = RestClient.delete("user/deleteUsers/" + vo.getId(), userId(), locale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            errors(r);
        }
    }

    public String profileBadge(Integer profileId) {
        if (profileId == null) {
            return "badge badge-muted";
        }
        switch (profileId) {
            case SessionMb.PROFILE_OPERATOR: return "badge badge-danger";
            case SessionMb.PROFILE_ADMIN: return "badge badge-warning";
            case SessionMb.PROFILE_EMPLOYEE: return "badge badge-info";
            default: return "badge badge-muted";
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

    public List<UserVo> getUsers() {
        return users;
    }

    public List<UserVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<UserVo> filtered) {
        this.filtered = filtered;
    }

    public List<ProfileVo> getAllProfiles() {
        return allProfiles;
    }

    public UserVo getSelected() {
        return selected;
    }

    public void setSelected(UserVo selected) {
        this.selected = selected;
    }
}
