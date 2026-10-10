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
 * Users of the business (USER_VIEW): BUSINESS_ADMIN (2) and EMPLOYEE (3) logins.
 * user/getUsers · user/saveUpdateUser · user/deleteUsers/{id} · profile/getProfiles
 */
@Named("userMb")
@ViewScoped
public class UserMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type USER_LIST = new TypeToken<List<UserVo>>() { }.getType();
    private static final Type PROFILE_LIST = new TypeToken<List<ProfileVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<UserVo> users = new ArrayList<>();
    private List<UserVo> filtered;
    private List<ProfileVo> profiles = new ArrayList<>();
    private UserVo selected = new UserVo();

    @PostConstruct
    public void init() {
        loadProfiles();
        refresh();
    }

    /** Only the business profiles (admin = 2, employee = 3) can be granted from here. */
    private void loadProfiles() {
        profiles = new ArrayList<>();
        try {
            List<ProfileVo> list = RestClient.get("profile/getProfiles", PROFILE_LIST, userId(), sessionMb.getLocale());
            if (list != null) {
                for (ProfileVo p : list) {
                    if (p.getId() != null && (p.getId() == SessionMb.PROFILE_ADMIN || p.getId() == SessionMb.PROFILE_EMPLOYEE)) {
                        profiles.add(p);
                    }
                }
            }
        } catch (RestException e) {
            // fall back to the two well-known profiles so the dialog still works
        }
        if (profiles.isEmpty()) {
            ProfileVo admin = new ProfileVo();
            admin.setId(SessionMb.PROFILE_ADMIN);
            admin.setName("BUSINESS_ADMIN");
            ProfileVo employee = new ProfileVo();
            employee.setId(SessionMb.PROFILE_EMPLOYEE);
            employee.setName("EMPLOYEE");
            profiles.add(admin);
            profiles.add(employee);
        }
    }

    public void refresh() {
        try {
            List<UserVo> list = RestClient.get("user/getUsers", USER_LIST, userId(), sessionMb.getLocale());
            users = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            users = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    public void prepareNew() {
        selected = new UserVo();
        selected.setProfileId(SessionMb.PROFILE_EMPLOYEE);
        selected.setPreferredLanguage(sessionMb.getLanguage());
    }

    public void prepareEdit(UserVo vo) {
        selected = vo;
        selected.setPassword(null);
    }

    public void save() {
        if (selected.getPassword() != null && selected.getPassword().isBlank()) {
            selected.setPassword(null);
        }
        if (selected.getId() == null && (selected.getPassword() == null || selected.getPassword().isBlank())) {
            warn(sessionMb.text("cat.user.password.required"));
            return;
        }
        RestResponse r = RestClient.post("user/saveUpdateUser", selected, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('userDialog').hide()");
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void delete(UserVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("user/deleteUsers/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.deleted"));
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public boolean isSelf(UserVo vo) {
        return vo != null && vo.getId() != null && vo.getId().equals(userId());
    }

    public String profileBadge(Integer profileId) {
        if (profileId == null) {
            return "badge badge-muted";
        }
        return profileId == SessionMb.PROFILE_ADMIN ? "badge badge-warning" : "badge badge-info";
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

    public List<UserVo> getUsers() {
        return users;
    }

    public List<UserVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<UserVo> filtered) {
        this.filtered = filtered;
    }

    public List<ProfileVo> getProfiles() {
        return profiles;
    }

    public UserVo getSelected() {
        return selected;
    }

    public void setSelected(UserVo selected) {
        this.selected = selected;
    }
}
