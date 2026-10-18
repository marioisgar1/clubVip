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
import com.prolog.clubvip.vo.ServiceVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Catalog of services offered by the business (SERVICE_VIEW).
 * CRUD against service/getServices, service/saveUpdateService and service/deleteServices/{id}.
 */
@Named("serviceMb")
@ViewScoped
public class ServiceMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type SERVICE_LIST = new TypeToken<List<ServiceVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<ServiceVo> services = new ArrayList<>();
    private List<ServiceVo> filtered;
    private ServiceVo selected = new ServiceVo();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<ServiceVo> list = RestClient.get("service/getServices", SERVICE_LIST, userId(), sessionMb.getLocale());
            services = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            services = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    public void prepareNew() {
        selected = new ServiceVo();
        selected.setActiveForBooking(Boolean.TRUE);
        selected.setDurationMinutes(30);
    }

    public void prepareEdit(ServiceVo vo) {
        selected = vo;
    }

    public void save() {
        // Who gives the service is chosen in the employee form; null tells the backend to leave it as it is
        // (sending the list loaded with this page could undo changes made there meanwhile).
        List<Integer> employeeIds = selected.getEmployeeIds();
        selected.setEmployeeIds(null);
        RestResponse r;
        try {
            r = RestClient.post("service/saveUpdateService", selected, userId(), sessionMb.getLocale());
        } finally {
            selected.setEmployeeIds(employeeIds);
        }
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('serviceDialog').hide()");
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void delete(ServiceVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("service/deleteServices/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.deleted"));
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    /** Names of the employees who give a service, or the "whole team" label when none is restricted. */
    public String whoGives(ServiceVo vo) {
        if (vo == null || vo.getEmployeeNames() == null || vo.getEmployeeNames().isEmpty()) {
            return sessionMb.text("cat.service.wholeTeam");
        }
        return String.join(", ", vo.getEmployeeNames());
    }

    public boolean isActive(ServiceVo vo) {
        return vo != null && Boolean.TRUE.equals(vo.getActiveForBooking());
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
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

    public List<ServiceVo> getServices() {
        return services;
    }

    public List<ServiceVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<ServiceVo> filtered) {
        this.filtered = filtered;
    }

    public ServiceVo getSelected() {
        return selected;
    }

    public void setSelected(ServiceVo selected) {
        this.selected = selected;
    }
}
