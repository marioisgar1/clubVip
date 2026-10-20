package com.prolog.clubvip.view;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.prolog.clubvip.constant.Constants;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

/**
 * Time zone of the GPS screens. The devices report in UTC; each viewer sees the times in the zone
 * of their computer (sent by the page with Intl.DateTimeFormat, see the gps pages) or in the one
 * they pick in the combo. Kept for the session, shared by the three GPS screens.
 */
@Named("gpsZoneMb")
@SessionScoped
public class GpsZoneMb implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Regions offered in the combo (the rest of the tz database are aliases or obscure). */
    private static final String[] REGIONS = {"America/", "Europe/", "Asia/", "Africa/", "Australia/", "Pacific/", "Atlantic/", "Indian/"};

    private String zone = Constants.TIMEZONE;
    /** True once the browser sent its zone or the user picked one: the page stops asking. */
    private boolean detected;
    private transient List<SelectItem> zones;

    /** Called by the page (p:remoteCommand) with the zone of the computer, only the first time. */
    public void detect() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String tz = params.get("tz");
        if (!detected && valid(tz)) {
            zone = tz;
        }
        detected = true;
    }

    /** The combo changed it. */
    public void changed() {
        if (!valid(zone)) {
            zone = Constants.TIMEZONE;
        }
        detected = true;
    }

    private static boolean valid(String tz) {
        try {
            return tz != null && !tz.isBlank() && ZoneId.of(tz) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public ZoneId getZoneId() {
        return valid(zone) ? ZoneId.of(zone) : ZoneId.of(Constants.TIMEZONE);
    }

    /** "UTC-06:00" of the chosen zone right now. */
    public String getOffset() {
        return offset(getZoneId());
    }

    private static String offset(ZoneId z) {
        ZoneOffset o = z.getRules().getOffset(Instant.now());
        return "UTC" + ("Z".equals(o.getId()) ? "+00:00" : o.getId());
    }

    /** Every zone of the main regions as "(UTC-06:00) America/Mexico_City", ordered by offset. */
    public List<SelectItem> getZones() {
        if (zones == null) {
            List<String> ids = new ArrayList<>();
            for (String id : ZoneId.getAvailableZoneIds()) {
                for (String r : REGIONS) {
                    if (id.startsWith(r)) {
                        ids.add(id);
                        break;
                    }
                }
            }
            if (!ids.contains("UTC")) {
                ids.add("UTC");
            }
            Instant now = Instant.now();
            ids.sort((a, b) -> {
                int c = Integer.compare(ZoneId.of(a).getRules().getOffset(now).getTotalSeconds(),
                        ZoneId.of(b).getRules().getOffset(now).getTotalSeconds());
                return c != 0 ? c : a.compareTo(b);
            });
            List<SelectItem> list = new ArrayList<>();
            for (String id : ids) {
                list.add(new SelectItem(id, "(" + offset(ZoneId.of(id)) + ") " + id.replace('_', ' ')));
            }
            zones = list;
        }
        return zones;
    }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public boolean isDetected() { return detected; }
}
