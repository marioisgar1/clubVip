package com.prolog.clubvip.security;

import java.io.IOException;
import java.util.Locale;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.vo.TrafficEventVo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Beacons of the public pages (traffic report). The browser cannot talk to the backend gateway
 * itself, so the page sends {@code /track?v=visitId&s=seconds} (time on the page, via
 * sendBeacon when the tab is hidden or closed) or {@code /track?v=visitId&e=WA_CLICK} (an event)
 * here and this forwards them. Always answers 204; nothing here may break the page.
 */
@WebServlet("/track")
public class TrackServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Locale ES = new Locale("es");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.setHeader("Cache-Control", "no-store");
        Integer visitId = intOf(request.getParameter("v"));
        if (visitId == null) {
            return;
        }
        try {
            String event = request.getParameter("e");
            if (event != null && !event.isBlank()) {
                RestClient.post("traffic/event", new TrafficEventVo(visitId, event.trim(), null, null), null, ES);
            }
            Integer seconds = intOf(request.getParameter("s"));
            if (seconds != null) {
                RestClient.post("traffic/heartbeat/" + visitId + "?seconds=" + seconds, null, null, ES);
            }
        } catch (RuntimeException e) {
            // a lost beacon is not worth an error
        }
    }

    private static Integer intOf(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
