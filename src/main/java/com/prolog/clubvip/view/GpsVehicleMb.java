package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.GpsClient;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.GpsCatalogVo;
import com.prolog.clubvip.vo.GpsDeviceVo;
import com.prolog.clubvip.vo.GpsVehicleVo;
import com.prolog.clubvip.vo.UserVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * GPS › Vehículos (Club VIP operator): name, plates, photo and IMEI of each vehicle, assigned to a
 * business administrator. The list also shows the live status of its IMEI from clubVipGps.
 */
@Named("gpsVehicleMb")
@ViewScoped
public class GpsVehicleMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<GpsVehicleVo>>() { }.getType();
    private static final Type USER_LIST_TYPE = new TypeToken<List<UserVo>>() { }.getType();
    private static final Type DEVICE_LIST_TYPE = new TypeToken<List<GpsDeviceVo>>() { }.getType();
    private static final Type PROFILE_LIST_TYPE = new TypeToken<List<com.prolog.clubvip.vo.GpsProfileVo>>() { }.getType();
    private static final Type CATALOG_LIST_TYPE = new TypeToken<List<GpsCatalogVo>>() { }.getType();
    /** Oldest model year offered (the backend accepts the same range). */
    private static final int MIN_YEAR = 1950;
    private static final Type CONFIG_LIST_TYPE = new TypeToken<List<com.prolog.clubvip.vo.GpsConfigVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    @Inject
    private GpsZoneMb gpsZoneMb;

    private List<GpsVehicleVo> vehicles = new ArrayList<>();
    private List<GpsVehicleVo> filtered;
    private List<UserVo> owners = new ArrayList<>();
    private Map<String, GpsDeviceVo> devices = new HashMap<>();
    /** Report profiles for the dialog, and the state of each IMEI's configuration in clubVipGps. */
    private List<com.prolog.clubvip.vo.GpsProfileVo> profiles = new ArrayList<>();
    private Map<String, com.prolog.clubvip.vo.GpsConfigVo> configs = new HashMap<>();
    private boolean gpsDown;
    private GpsVehicleVo selected = new GpsVehicleVo();
    /** Brand / model catalogs; the operator adds to them from the vehicle dialog. */
    private List<GpsCatalogVo> brands = new ArrayList<>();
    private List<GpsCatalogVo> allModels = new ArrayList<>();
    private List<Integer> years = new ArrayList<>();
    /** "brand" or "model": what the small add dialog creates, and the name typed in it. */
    private String catalogMode;
    private String catalogName;
    /** Data URL of a newly picked photo (hidden textarea filled by the page script). */
    private String newPhotoBase64;
    /** Goes into the photo URLs so the browser fetches them again after a save. */
    private long photoVersion = System.currentTimeMillis();

    @PostConstruct
    public void init() {
        refresh();
        loadOwners();
        loadProfiles();
        loadCatalogs();
        for (int y = java.time.Year.now().getValue() + 1; y >= MIN_YEAR; y--) {
            years.add(y);
        }
    }

    private void loadCatalogs() {
        try {
            List<GpsCatalogVo> b = RestClient.get("gpsVehicle/getBrands", CATALOG_LIST_TYPE, userId(), sessionMb.getLocale());
            List<GpsCatalogVo> m = RestClient.get("gpsVehicle/getModels", CATALOG_LIST_TYPE, userId(), sessionMb.getLocale());
            brands = b != null ? b : new ArrayList<>();
            allModels = m != null ? m : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Models of the brand chosen in the dialog. */
    public List<GpsCatalogVo> getModels() {
        List<GpsCatalogVo> result = new ArrayList<>();
        if (selected.getBrandId() != null) {
            for (GpsCatalogVo m : allModels) {
                if (selected.getBrandId().equals(m.getBrandId())) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /** The brand changed: its model no longer applies. */
    public void brandChanged() {
        selected.setModelId(null);
    }

    public void prepareBrand() {
        catalogMode = "brand";
        catalogName = null;
    }

    public void prepareModel() {
        catalogMode = "model";
        catalogName = null;
    }

    /** Adds the brand or model typed in the small dialog and selects it in the vehicle. */
    public void saveCatalog() {
        boolean brand = "brand".equals(catalogMode);
        GpsCatalogVo payload = new GpsCatalogVo();
        payload.setName(catalogName != null ? catalogName.trim() : null);
        if (!brand) {
            payload.setBrandId(selected.getBrandId());
        }
        try {
            GpsCatalogVo saved = RestClient.post(brand ? "gpsVehicle/saveBrand" : "gpsVehicle/saveModel", payload,
                    GpsCatalogVo.class, userId(), sessionMb.getLocale());
            loadCatalogs();
            if (saved != null) {
                if (brand) {
                    selected.setBrandId(saved.getId());
                    selected.setModelId(null);
                } else {
                    selected.setModelId(saved.getId());
                }
            }
            info(sessionMb.text(brand ? "gps.brand.added" : "gps.model.added"));
            PrimeFaces.current().executeScript("PF('catalogDialog').hide()");
        } catch (RestException e) {
            errors(e);
        }
    }

    public String getCatalogTitle() {
        return sessionMb.text("model".equals(catalogMode) ? "gps.model.new" : "gps.brand.new");
    }

    /** Brand the new model goes to (shown in the add dialog). */
    public String getSelectedBrandName() {
        for (GpsCatalogVo b : brands) {
            if (b.getId().equals(selected.getBrandId())) {
                return b.getName();
            }
        }
        return "";
    }

    private void loadProfiles() {
        try {
            List<com.prolog.clubvip.vo.GpsProfileVo> list = RestClient.get("gpsProfile/getProfiles", PROFILE_LIST_TYPE, userId(), sessionMb.getLocale());
            profiles = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void refresh() {
        try {
            List<GpsVehicleVo> list = RestClient.get("gpsVehicle/getVehicles", LIST_TYPE, userId(), sessionMb.getLocale());
            vehicles = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
        loadDevices();
        filtered = null;
    }

    private void loadDevices() {
        Map<String, GpsDeviceVo> map = new HashMap<>();
        try {
            List<GpsDeviceVo> list = GpsClient.get("devices", DEVICE_LIST_TYPE);
            if (list != null) {
                for (GpsDeviceVo d : list) {
                    map.put(d.getImei(), d);
                }
            }
            gpsDown = false;
        } catch (RestException e) {
            gpsDown = true; // the catalog still works; the status column says the service is down
        }
        devices = map;
        Map<String, com.prolog.clubvip.vo.GpsConfigVo> cfg = new HashMap<>();
        if (!gpsDown) {
            try {
                List<com.prolog.clubvip.vo.GpsConfigVo> list = GpsClient.get("configs", CONFIG_LIST_TYPE);
                if (list != null) {
                    for (com.prolog.clubvip.vo.GpsConfigVo c : list) {
                        cfg.put(c.getImei(), c);
                    }
                }
            } catch (RestException e) {
                // an older clubVipGps without configs: the profile column shows the name only
            }
        }
        configs = cfg;
    }

    /** Active business administrators: the possible owners. */
    private void loadOwners() {
        List<UserVo> result = new ArrayList<>();
        try {
            List<UserVo> list = RestClient.get("user/getUsers", USER_LIST_TYPE, userId(), sessionMb.getLocale());
            if (list != null) {
                for (UserVo u : list) {
                    if (u.getProfileId() != null && u.getProfileId() == SessionMb.PROFILE_ADMIN
                            && (u.getStatus() == null || u.getStatus() == 1)) {
                        result.add(u);
                    }
                }
            }
        } catch (RestException e) {
            errors(e);
        }
        result.sort((a, b) -> label(a).compareToIgnoreCase(label(b)));
        owners = result;
    }

    public void prepareNew() {
        selected = new GpsVehicleVo();
        newPhotoBase64 = null;
    }

    public void prepareEdit(GpsVehicleVo vo) {
        newPhotoBase64 = null;
        try {
            selected = RestClient.get("gpsVehicle/getVehicle/" + vo.getId(), GpsVehicleVo.class, userId(), sessionMb.getLocale());
        } catch (RestException e) {
            errors(e);
            selected = new GpsVehicleVo();
        }
    }

    public void removePhoto() {
        selected.setPhotoBase64(null);
        selected.setHasPhoto(false);
        selected.setRemovePhoto(true);
        newPhotoBase64 = null;
    }

    public void save() {
        GpsVehicleVo payload = new GpsVehicleVo();
        payload.setId(selected.getId());
        payload.setName(selected.getName());
        payload.setPlates(selected.getPlates());
        payload.setBrandId(selected.getBrandId());
        payload.setModelId(selected.getBrandId() != null ? selected.getModelId() : null);
        payload.setModelYear(selected.getModelYear());
        payload.setImei(selected.getImei() != null ? selected.getImei().trim() : null);
        payload.setOwnerUserId(selected.getOwnerUserId());
        payload.setProfileId(selected.getProfileId());
        if (newPhotoBase64 != null && !newPhotoBase64.isBlank()) {
            payload.setPhotoBase64(ProductMb.stripDataUri(newPhotoBase64.trim()));
        } else if (Boolean.TRUE.equals(selected.getRemovePhoto())) {
            payload.setRemovePhoto(true);
        }
        RestResponse r = RestClient.post("gpsVehicle/saveUpdateVehicle", payload, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            com.prolog.clubvip.vo.GpsProfileSaveVo res = parse(r.getBody());
            if (res != null && res.getFailed() != null && res.getFailed() > 0) {
                warn(sessionMb.text("gps.vehicle.configNotSent"));
            } else if (res != null && res.getSent() != null && res.getSent() > 0) {
                info(sessionMb.text("gps.vehicle.savedConfig"));
            } else {
                info(sessionMb.text("msg.saved"));
            }
            PrimeFaces.current().executeScript("PF('vehicleDialog').hide()");
            newPhotoBase64 = null;
            photoVersion = System.currentTimeMillis();
            refresh();
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    public void delete(GpsVehicleVo vo) {
        RestResponse r = RestClient.delete("gpsVehicle/deleteVehicles/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    /** Sends the vehicle's profile to its device again (e.g. after it failed or the device was reset). */
    public void resend(GpsVehicleVo vo) {
        try {
            com.prolog.clubvip.vo.GpsProfileSaveVo res = RestClient.post("gpsProfile/resendVehicle/" + vo.getId(), null,
                    com.prolog.clubvip.vo.GpsProfileSaveVo.class, userId(), sessionMb.getLocale());
            if (res != null && res.getFailed() != null && res.getFailed() > 0) {
                warn(sessionMb.text("gps.vehicle.configNotSent"));
            } else {
                info(sessionMb.text("gps.vehicle.configResent"));
            }
        } catch (RestException e) {
            errors(e);
        }
        loadDevices();
    }

    private static com.prolog.clubvip.vo.GpsProfileSaveVo parse(String body) {
        try {
            return body == null || body.isBlank() ? null
                    : new com.google.gson.Gson().fromJson(body, com.prolog.clubvip.vo.GpsProfileSaveVo.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ report profile of the device

    /** "Rastreo fino · En movimiento 15 s / Detenido 5 min" for the profile list of the dialog. */
    public String profileLabel(com.prolog.clubvip.vo.GpsProfileVo p) {
        return p.getName() + " · " + sessionMb.text("gps.profile.moving.short") + " " + GpsProfileMb.duration(p.getMovingIntervalSec())
                + " / " + sessionMb.text("gps.profile.parked.short") + " " + GpsProfileMb.duration(p.getParkedIntervalSec());
    }

    public com.prolog.clubvip.vo.GpsConfigVo config(String imei) {
        return imei != null ? configs.get(imei) : null;
    }

    /**
     * APPLIED (the device answered), SENT (waiting for its answer), WAITING (sent as soon as it
     * connects), DISABLED (sending is off in clubVipGps), FAILED, or "" when there is nothing to show.
     */
    public String configStatus(GpsVehicleVo v) {
        if (v.getProfileId() == null || gpsDown) return "";
        com.prolog.clubvip.vo.GpsConfigVo c = config(v.getImei());
        if (c == null) return "UNKNOWN";
        if (!v.getProfileId().equals(c.getProfileId())) return "WAITING";
        switch (c.getStatus() != null ? c.getStatus() : "") {
            case "CONFIRMED": return "APPLIED";
            case "SENT": return "SENT";
            case "FAILED": return "FAILED";
            default: return Boolean.FALSE.equals(c.getSendingEnabled()) ? "DISABLED" : "WAITING";
        }
    }

    public String configBadge(GpsVehicleVo v) {
        switch (configStatus(v)) {
            case "APPLIED": return "badge badge-success";
            case "SENT": return "badge badge-info";
            case "FAILED": return "badge badge-danger";
            case "WAITING": case "DISABLED": return "badge badge-warning";
            default: return "badge badge-muted";
        }
    }

    public String configText(GpsVehicleVo v) {
        String s = configStatus(v);
        return s.isEmpty() ? "" : sessionMb.text("gps.config." + s);
    }

    /** Line under the badge: when it was confirmed / sent, or why it is still pending. */
    public String configHint(GpsVehicleVo v) {
        com.prolog.clubvip.vo.GpsConfigVo c = config(v.getImei());
        if (c == null || v.getProfileId() == null) return "";
        switch (configStatus(v)) {
            case "APPLIED": return GpsFmt.dateTime(c.getConfirmedAt(), gpsZoneMb.getZoneId());
            case "SENT": return sessionMb.text("gps.config.attempt").replace("{0}", String.valueOf(c.getAttempts()));
            case "FAILED": case "DISABLED": return c.getLastError() != null ? c.getLastError() : "";
            default: return c.getLastError() != null ? c.getLastError() : sessionMb.text("gps.config.WAITING.hint");
        }
    }

    // ------------------------------------------------------------------ live status of the IMEI

    public GpsDeviceVo device(String imei) {
        return imei != null ? devices.get(imei) : null;
    }

    /** ONLINE / SILENT / OFFLINE, NEVER (the IMEI never reported) or DOWN (service unreachable). */
    public String status(String imei) {
        if (gpsDown) return "DOWN";
        GpsDeviceVo d = device(imei);
        return d != null && d.getStatus() != null ? d.getStatus() : "NEVER";
    }

    public String statusBadge(String imei) {
        return GpsDeviceMb.badge(status(imei));
    }

    public String statusText(String imei) {
        return sessionMb.text("gps.status." + status(imei));
    }

    /** "hace 3 min" / "desconectado hace 2 h": the line under the badge. */
    public String statusHint(String imei) {
        GpsDeviceVo d = device(imei);
        if (d == null) return "";
        if (Boolean.TRUE.equals(d.getConnected())) {
            return sessionMb.text("gps.lastFrame") + " " + GpsFmt.duration(d.getSecondsSinceLastFrame());
        }
        return sessionMb.text("gps.offlineFor") + " " + GpsFmt.duration(d.getDisconnectedForSeconds());
    }

    public String ownerLabel(UserVo u) {
        return label(u);
    }

    private static String label(UserVo u) {
        String s = u.getFullName() != null ? u.getFullName() : "";
        if (u.getBusinessName() != null && !u.getBusinessName().isBlank()) {
            s += " · " + u.getBusinessName();
        }
        return s;
    }

    // ------------------------------------------------------------------ helpers

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void errors(RestException e) {
        for (String m : e.getMessages()) {
            error(m);
        }
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void warn(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // ------------------------------------------------------------------ getters / setters

    public List<GpsVehicleVo> getVehicles() { return vehicles; }
    public List<GpsVehicleVo> getFiltered() { return filtered; }
    public void setFiltered(List<GpsVehicleVo> filtered) { this.filtered = filtered; }
    public List<UserVo> getOwners() { return owners; }
    public List<GpsCatalogVo> getBrands() { return brands; }
    public List<Integer> getYears() { return years; }
    public String getCatalogMode() { return catalogMode; }
    public String getCatalogName() { return catalogName; }
    public void setCatalogName(String catalogName) { this.catalogName = catalogName; }
    public List<com.prolog.clubvip.vo.GpsProfileVo> getProfiles() { return profiles; }
    public boolean isGpsDown() { return gpsDown; }
    public GpsVehicleVo getSelected() { return selected; }
    public void setSelected(GpsVehicleVo selected) { this.selected = selected; }
    public String getNewPhotoBase64() { return newPhotoBase64; }
    public void setNewPhotoBase64(String newPhotoBase64) { this.newPhotoBase64 = newPhotoBase64; }
    public long getPhotoVersion() { return photoVersion; }
}
