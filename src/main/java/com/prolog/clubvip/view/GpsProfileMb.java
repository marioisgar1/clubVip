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
import com.prolog.clubvip.vo.GpsProfileSaveVo;
import com.prolog.clubvip.vo.GpsProfileVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * GPS › Perfiles (Club VIP operator): how often and why the Suntech devices report. The dialog works
 * on a friendly form (interval + unit, on/off switch + value) that becomes the backend's numbers
 * (seconds, 0 = off). Saving a profile in use sends it to all its vehicles.
 */
@Named("gpsProfileMb")
@ViewScoped
public class GpsProfileMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type LIST_TYPE = new TypeToken<List<GpsProfileVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<GpsProfileVo> profiles = new ArrayList<>();
    private GpsProfileVo selected = new GpsProfileVo();

    // ---- dialog form
    private Integer movingValue;
    private String movingUnit = "s";
    private Integer parkedValue;
    private String parkedUnit = "min";
    private boolean distanceOn;
    private Integer distanceM;
    private boolean angleOn;
    private Integer angleDeg;
    private boolean speedOn;
    private Integer speedKmh;
    private boolean idleOn;
    private Integer idleMin;
    private boolean keepAliveOn;
    private Integer keepAliveMin;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            List<GpsProfileVo> list = RestClient.get("gpsProfile/getProfiles", LIST_TYPE, userId(), sessionMb.getLocale());
            profiles = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            errors(e);
        }
    }

    /** A new profile starts as the usual one: every minute moving, every 15 min stopped, 30° turns. */
    public void prepareNew() {
        GpsProfileVo p = new GpsProfileVo();
        p.setMovingIntervalSec(60);
        p.setParkedIntervalSec(900);
        p.setDistanceM(0);
        p.setAngleDeg(30);
        p.setKeepAliveMin(15);
        p.setSpeedLimitKmh(0);
        p.setIdleAlertMin(0);
        p.setIgnitionReport(true);
        p.setHarshDriving(false);
        p.setSleepWhenParked(false);
        p.setVehicleCount(0L);
        toForm(p);
    }

    public void prepareEdit(GpsProfileVo vo) {
        try {
            toForm(RestClient.get("gpsProfile/getProfile/" + vo.getId(), GpsProfileVo.class, userId(), sessionMb.getLocale()));
        } catch (RestException e) {
            errors(e);
        }
    }

    /** Copy of a profile as a starting point ("Rastreo fino (copia)"). */
    public void prepareCopy(GpsProfileVo vo) {
        prepareEdit(vo);
        selected.setId(null);
        selected.setName(selected.getName() + " " + sessionMb.text("gps.profile.copySuffix"));
        selected.setVehicleCount(0L);
    }

    public void save() {
        GpsProfileVo p = new GpsProfileVo();
        p.setId(selected.getId());
        p.setName(selected.getName());
        p.setDescription(selected.getDescription());
        p.setMovingIntervalSec(seconds(movingValue, movingUnit));
        p.setParkedIntervalSec(seconds(parkedValue, parkedUnit));
        p.setDistanceM(distanceOn ? distanceM : 0);
        p.setAngleDeg(angleOn ? angleDeg : 0);
        p.setSpeedLimitKmh(speedOn ? speedKmh : 0);
        p.setIdleAlertMin(idleOn ? idleMin : 0);
        p.setKeepAliveMin(keepAliveOn ? keepAliveMin : 0);
        p.setIgnitionReport(Boolean.TRUE.equals(selected.getIgnitionReport()));
        p.setHarshDriving(Boolean.TRUE.equals(selected.getHarshDriving()));
        p.setSleepWhenParked(Boolean.TRUE.equals(selected.getSleepWhenParked()));
        try {
            GpsProfileSaveVo r = RestClient.post("gpsProfile/saveUpdateProfile", p, GpsProfileSaveVo.class, userId(), sessionMb.getLocale());
            int vehicles = r != null && r.getVehicles() != null ? r.getVehicles() : 0;
            int failed = r != null && r.getFailed() != null ? r.getFailed() : 0;
            if (vehicles == 0) {
                info(sessionMb.text("msg.saved"));
            } else if (failed == 0) {
                info(sessionMb.text("gps.profile.savedSent").replace("{0}", String.valueOf(vehicles)));
            } else {
                warn(sessionMb.text("gps.profile.savedFailed").replace("{0}", String.valueOf(failed)));
            }
            PrimeFaces.current().executeScript("PF('profileDialog').hide()");
            refresh();
        } catch (RestException e) {
            errors(e);
        }
    }

    public void delete(GpsProfileVo vo) {
        RestResponse r = RestClient.delete("gpsProfile/deleteProfiles/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            info(sessionMb.text("msg.deleted"));
            refresh();
        } else {
            for (String m : RestClient.extractErrors(r.getBody())) {
                error(m);
            }
        }
    }

    // ------------------------------------------------------------------ list texts

    /** "cada 15 s", "cada 5 min", "cada 1 h 30 min". */
    public String every(Integer seconds) {
        return sessionMb.text("gps.profile.every") + " " + duration(seconds);
    }

    public static String duration(Integer seconds) {
        if (seconds == null || seconds <= 0) return "-";
        if (seconds < 60) return seconds + " s";
        int min = seconds / 60;
        if (min < 60) return min + " min" + (seconds % 60 > 0 ? " " + seconds % 60 + " s" : "");
        int h = min / 60;
        return h + " h" + (min % 60 > 0 ? " " + min % 60 + " min" : "");
    }

    /** Line under the moving interval of the dialog: what it means per hour while driving. */
    public String getMovingHint() {
        Integer s = seconds(movingValue, movingUnit);
        if (s == null || s <= 0) return "";
        return sessionMb.text("gps.profile.perHour").replace("{0}", String.valueOf(Math.round(3600.0 / s)));
    }

    public String getParkedHint() {
        Integer s = seconds(parkedValue, parkedUnit);
        if (s == null || s <= 0) return "";
        return sessionMb.text("gps.profile.perDay").replace("{0}", String.valueOf(Math.round(86400.0 / s)));
    }

    // ------------------------------------------------------------------ form <-> profile

    private void toForm(GpsProfileVo p) {
        selected = p;
        int moving = p.getMovingIntervalSec() != null ? p.getMovingIntervalSec() : 60;
        movingUnit = unit(moving);
        movingValue = value(moving, movingUnit);
        int parked = p.getParkedIntervalSec() != null ? p.getParkedIntervalSec() : 900;
        parkedUnit = unit(parked);
        parkedValue = value(parked, parkedUnit);
        distanceOn = positive(p.getDistanceM());
        distanceM = distanceOn ? p.getDistanceM() : 200;
        angleOn = positive(p.getAngleDeg());
        angleDeg = angleOn ? p.getAngleDeg() : 30;
        speedOn = positive(p.getSpeedLimitKmh());
        speedKmh = speedOn ? p.getSpeedLimitKmh() : 110;
        idleOn = positive(p.getIdleAlertMin());
        idleMin = idleOn ? p.getIdleAlertMin() : 10;
        keepAliveOn = positive(p.getKeepAliveMin());
        keepAliveMin = keepAliveOn ? p.getKeepAliveMin() : 15;
    }

    /** The largest unit that represents the interval exactly (3600 → h, 900 → min, 45 → s). */
    private static String unit(int seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) return "h";
        if (seconds >= 60 && seconds % 60 == 0) return "min";
        return "s";
    }

    private static int value(int seconds, String unit) {
        return "h".equals(unit) ? seconds / 3600 : "min".equals(unit) ? seconds / 60 : seconds;
    }

    private static Integer seconds(Integer value, String unit) {
        if (value == null) return null;
        return "h".equals(unit) ? value * 3600 : "min".equals(unit) ? value * 60 : value;
    }

    private static boolean positive(Integer v) {
        return v != null && v > 0;
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

    public List<GpsProfileVo> getProfiles() { return profiles; }
    public GpsProfileVo getSelected() { return selected; }
    public void setSelected(GpsProfileVo selected) { this.selected = selected; }
    public Integer getMovingValue() { return movingValue; }
    public void setMovingValue(Integer movingValue) { this.movingValue = movingValue; }
    public String getMovingUnit() { return movingUnit; }
    public void setMovingUnit(String movingUnit) { this.movingUnit = movingUnit; }
    public Integer getParkedValue() { return parkedValue; }
    public void setParkedValue(Integer parkedValue) { this.parkedValue = parkedValue; }
    public String getParkedUnit() { return parkedUnit; }
    public void setParkedUnit(String parkedUnit) { this.parkedUnit = parkedUnit; }
    public boolean isDistanceOn() { return distanceOn; }
    public void setDistanceOn(boolean distanceOn) { this.distanceOn = distanceOn; }
    public Integer getDistanceM() { return distanceM; }
    public void setDistanceM(Integer distanceM) { this.distanceM = distanceM; }
    public boolean isAngleOn() { return angleOn; }
    public void setAngleOn(boolean angleOn) { this.angleOn = angleOn; }
    public Integer getAngleDeg() { return angleDeg; }
    public void setAngleDeg(Integer angleDeg) { this.angleDeg = angleDeg; }
    public boolean isSpeedOn() { return speedOn; }
    public void setSpeedOn(boolean speedOn) { this.speedOn = speedOn; }
    public Integer getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Integer speedKmh) { this.speedKmh = speedKmh; }
    public boolean isIdleOn() { return idleOn; }
    public void setIdleOn(boolean idleOn) { this.idleOn = idleOn; }
    public Integer getIdleMin() { return idleMin; }
    public void setIdleMin(Integer idleMin) { this.idleMin = idleMin; }
    public boolean isKeepAliveOn() { return keepAliveOn; }
    public void setKeepAliveOn(boolean keepAliveOn) { this.keepAliveOn = keepAliveOn; }
    public Integer getKeepAliveMin() { return keepAliveMin; }
    public void setKeepAliveMin(Integer keepAliveMin) { this.keepAliveMin = keepAliveMin; }
}
