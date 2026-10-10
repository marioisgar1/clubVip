package com.prolog.clubvip.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ImageVo;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Main photo of a product for the product list ({@code /productPhoto?id=productId&v=n}). The list
 * loads products without images (hundreds of photos would make it heavy), so each visible row
 * asks for its thumbnail here; the backend call runs as the logged user, so it only answers for
 * products of the user's own business. {@code v} changes after a save, so the browser can keep
 * the image cached meanwhile. 404 when there is no session or the product has no photo.
 */
@WebServlet("/productPhoto")
public class ProductPhotoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object u = session != null ? session.getAttribute(Constants.SESSION_USER) : null;
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        Integer productId = parseId(request.getParameter("id"));
        if (user == null || productId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] photo = null;
        try {
            Locale locale = new Locale(user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "es");
            ImageVo vo = RestClient.get("product/getPrimaryPhoto/" + productId, ImageVo.class, user.getUserId(), locale);
            if (vo != null && vo.getImageBase64() != null && !vo.getImageBase64().isBlank()) {
                photo = Base64.getMimeDecoder().decode(vo.getImageBase64());
            }
        } catch (RestException | IllegalArgumentException e) {
            photo = null; // another business's product, or a broken image: show the placeholder
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
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
