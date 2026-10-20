package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.GpsClient;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.GpsDeviceVo;
import com.prolog.clubvip.vo.GpsVehicleVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * GPS › Equipos conectados (Club VIP operator): every IMEI the receiver knows, joined by IMEI with
 * the vehicles of the catalog, plus the vehicles whose GPS never reported. Refreshes itself.
 */
@Named("gpsDeviceMb")
@ViewScoped
public class GpsDeviceMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type DEVICE_LIST_TYPE = new TypeToken<List<GpsDeviceVo>>() { }.getType();
    private static final Type VEHICLE_LIST_TYPE = new TypeToken<List<GpsVehicleVo>>() { }.getType();
    private static final Type FRAME_LIST_TYPE = new TypeToken<List<Map<String, Object>>>() { }.getType();

    /** One line of the screen: the device (null = never reported) and its vehicle (null = not registered). */
    public static class Row implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String imei;
        private final GpsDeviceVo device;
        private final GpsVehicleVo vehicle;
        private final String status;

        Row(String imei, GpsDeviceVo device, GpsVehicleVo vehicle) {
            this.imei = imei;
            this.device = device;
            this.vehicle = vehicle;
            this.status = device != null && device.getStatus() != null ? device.getStatus() : "NEVER";
        }

        public String getImei() { return imei; }
        public GpsDeviceVo getDevice() { return device; }
        public GpsVehicleVo getVehicle() { return vehicle; }
        public String getStatus() { return status; }

        /** Text the table filter searches: IMEI, vehicle, plates, owner. */
        public String getSearch() {
            StringBuilder sb = new StringBuilder(imei);
            if (vehicle != null) {
                sb.append(' ').append(s(vehicle.getName())).append(' ').append(s(vehicle.getPlates()))
                        .append(' ').append(s(vehicle.getOwnerName())).append(' ').append(s(vehicle.getOwnerBusinessName()));
            }
            return sb.toString();
        }

        private static String s(String v) {
            return v != null ? v : "";
        }
    }

    @Inject
    private SessionMb sessionMb;

    @Inject
    private GpsZoneMb gpsZoneMb;

    private List<Row> rows = new ArrayList<>();
    private List<Row> filtered;
    private String statusFilter = "ALL";
    private boolean gpsDown;
    private String gpsError;
    private int online;
    private int silent;
    private int offline;
    private int never;
    private int unregistered;
    private java.time.Instant updatedAt;

    private Row selected;
    private List<Map<String, Object>> frames = new ArrayList<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        Map<String, GpsVehicleVo> vehicles = new HashMap<>();
        try {
            List<GpsVehicleVo> list = RestClient.get("gpsVehicle/getVehicles", VEHICLE_LIST_TYPE, sessionMb.getUserId(), sessionMb.getLocale());
            if (list != null) {
                for (GpsVehicleVo v : list) {
                    vehicles.put(v.getImei(), v);
                }
            }
        } catch (RestException e) {
            for (String m : e.getMessages()) {
                error(m);
            }
        }
        List<GpsDeviceVo> devices = new ArrayList<>();
        try {
            List<GpsDeviceVo> list = GpsClient.get("devices", DEVICE_LIST_TYPE);
            devices = list != null ? list : devices;
            gpsDown = false;
            gpsError = null;
        } catch (RestException e) {
            gpsDown = true;
            gpsError = String.join(" ", e.getMessages());
        }
        List<Row> result = new ArrayList<>();
        online = silent = offline = never = unregistered = 0;
        for (GpsDeviceVo d : devices) {
            GpsVehicleVo v = vehicles.remove(d.getImei());
            if (v == null) unregistered++;
            result.add(new Row(d.getImei(), d, v));
        }
        if (!gpsDown) {
            for (GpsVehicleVo v : vehicles.values()) {
                result.add(new Row(v.getImei(), null, v));
            }
        }
        for (Row r : result) {
            switch (r.getStatus()) {
                case "ONLINE": online++; break;
                case "SILENT": silent++; break;
                case "OFFLINE": offline++; break;
                default: never++;
            }
        }
        // Online first; inside each status the one that reported most recently first.
        result.sort((a, b) -> {
            int c = Integer.compare(rank(a.getStatus()), rank(b.getStatus()));
            if (c != 0) return c;
            long sa = a.getDevice() != null && a.getDevice().getSecondsSinceLastFrame() != null ? a.getDevice().getSecondsSinceLastFrame() : Long.MAX_VALUE;
            long sb = b.getDevice() != null && b.getDevice().getSecondsSinceLastFrame() != null ? b.getDevice().getSecondsSinceLastFrame() : Long.MAX_VALUE;
            return Long.compare(sa, sb);
        });
        rows = result;
        filtered = null;
        updatedAt = java.time.Instant.now();
    }

    public void filter(String status) {
        statusFilter = status;
        filtered = null;
    }

    public List<Row> getVisibleRows() {
        if ("ALL".equals(statusFilter)) {
            return rows;
        }
        List<Row> list = new ArrayList<>();
        for (Row r : rows) {
            if (statusFilter.equals(r.getStatus())) list.add(r);
        }
        return list;
    }

    public void showFrames(Row row) {
        selected = row;
        frames = new ArrayList<>();
        try {
            List<Map<String, Object>> list = GpsClient.get("devices/" + RestClient.encode(row.getImei()) + "/frames?limit=50", FRAME_LIST_TYPE);
            frames = list != null ? list : frames;
        } catch (RestException e) {
            for (String m : e.getMessages()) {
                error(m);
            }
        }
    }

    // ------------------------------------------------------------------ view helpers

    static int rank(String status) {
        switch (status) {
            case "ONLINE": return 0;
            case "SILENT": return 1;
            case "OFFLINE": return 2;
            default: return 3;
        }
    }

    /** Badge class of a status (shared with the vehicles screen). */
    static String badge(String status) {
        switch (status) {
            case "ONLINE": return "badge badge-success";
            case "SILENT": return "badge badge-warning";
            case "OFFLINE": return "badge badge-danger";
            default: return "badge badge-muted";
        }
    }

    public String statusBadge(String status) {
        return badge(status);
    }

    public String statusText(String status) {
        return sessionMb.text("gps.status." + status);
    }

    public String dateTime(Object iso) {
        return GpsFmt.dateTime(iso != null ? String.valueOf(iso) : null, gpsZoneMb.getZoneId());
    }

    public String duration(Long seconds) {
        return GpsFmt.duration(seconds);
    }

    /** Google Maps link of a position; null without position. */
    public String mapsUrl(Object lat, Object lon) {
        if (lat == null || lon == null) return null;
        return "https://www.google.com/maps?q=" + lat + "," + lon;
    }

    public String coords(Object lat, Object lon) {
        if (lat == null || lon == null) return "—";
        return String.format(Locale.ROOT, "%.6f, %.6f", toDouble(lat), toDouble(lon));
    }

    public String speed(Object v) {
        return v == null ? "—" : String.format(Locale.ROOT, "%.0f km/h", toDouble(v));
    }

    public String volts(Object v) {
        return v == null ? "—" : String.format(Locale.ROOT, "%.2f V", toDouble(v));
    }

    /** Odometer in meters as km. */
    public String km(Object meters) {
        return meters == null ? "—" : String.format(Locale.ROOT, "%,.1f km", toDouble(meters) / 1000.0);
    }

    /** Whole number of a JSON value (Gson reads them as Double: 14.0 -> "14"). */
    public String num(Object v) {
        if (v == null) return "—";
        return v instanceof Number ? String.valueOf(((Number) v).longValue()) : String.valueOf(v);
    }

    public boolean isTrue(Object v) {
        return v != null && ("true".equals(String.valueOf(v)) || "1".equals(String.valueOf(v)) || "1.0".equals(String.valueOf(v)));
    }

    private static double toDouble(Object v) {
        return v instanceof Number ? ((Number) v).doubleValue() : Double.parseDouble(String.valueOf(v));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // ------------------------------------------------------------------ getters / setters

    public List<Row> getRows() { return rows; }
    public List<Row> getFiltered() { return filtered; }
    public void setFiltered(List<Row> filtered) { this.filtered = filtered; }
    public String getStatusFilter() { return statusFilter; }
    public boolean isGpsDown() { return gpsDown; }
    public String getGpsError() { return gpsError; }
    public int getOnline() { return online; }
    public int getSilent() { return silent; }
    public int getOffline() { return offline; }
    public int getNever() { return never; }
    public int getUnregistered() { return unregistered; }
    public int getTotal() { return rows.size(); }
    /** Time of the last refresh in the chosen zone. */
    public String getUpdatedAt() {
        return updatedAt == null ? "" : java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").format(updatedAt.atZone(gpsZoneMb.getZoneId()));
    }
    public Row getSelected() { return selected; }
    public List<Map<String, Object>> getFrames() { return frames; }
}
