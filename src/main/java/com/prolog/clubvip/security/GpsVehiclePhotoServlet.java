package com.prolog.clubvip.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.GpsVehicleVo;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Photo of a GPS vehicle ({@code /gpsVehiclePhoto?id=vehicleId&v=n}) for the GPS screens; the list
 * omits photos. The backend only answers to the Club VIP operator. {@code v} busts the browser
 * cache after an edit. 404 when there is no session or no photo.
 */
@WebServlet("/gpsVehiclePhoto")
public class GpsVehiclePhotoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object u = session != null ? session.getAttribute(Constants.SESSION_USER) : null;
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        Integer id = parseId(request.getParameter("id"));
        byte[] photo = null;
        if (user != null && id != null) {
            try {
                GpsVehicleVo vo = RestClient.get("gpsVehicle/getVehicle/" + id, GpsVehicleVo.class, user.getUserId(), new Locale("es"));
                if (vo != null && vo.getPhotoBase64() != null && !vo.getPhotoBase64().isBlank()) {
                    photo = Base64.getMimeDecoder().decode(vo.getPhotoBase64());
                }
            } catch (RestException | IllegalArgumentException e) {
                photo = null;
            }
        }
        if (photo == null || photo.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(BusinessLogo.contentType(photo));
        response.setHeader("Cache-Control", "private, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(photo.length);
        response.getOutputStream().write(photo);
    }

    private static Integer parseId(String value) {
        try {
            return value != null ? Integer.valueOf(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
