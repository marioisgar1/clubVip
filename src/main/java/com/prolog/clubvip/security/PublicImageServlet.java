package com.prolog.clubvip.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ImageVo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Images of the public pages, no session needed: {@code /publicImage?logo=businessId} (business
 * logo), {@code /publicImage?photo=productId} (main photo of a store product),
 * {@code /publicImage?storePhoto=photoId} (any photo of a store product, detail gallery) and
 * {@code /publicImage?pagePhoto=photoId} (cover / gallery of "Mi página"). The backend only
 * answers for active businesses (and, for photos, with the sales module on). 404 when there is none.
 */
@WebServlet("/publicImage")
public class PublicImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer logo = parseId(request.getParameter("logo"));
        Integer photo = parseId(request.getParameter("photo"));
        Integer pagePhoto = parseId(request.getParameter("pagePhoto"));
        Integer storePhoto = parseId(request.getParameter("storePhoto"));
        String path = logo != null ? "business/getLogo/" + logo
                : photo != null ? "product/getStorePhoto/" + photo
                : storePhoto != null ? "product/getStorePhotoById/" + storePhoto
                : pagePhoto != null ? "businessPage/getPhoto/" + pagePhoto : null;
        byte[] bytes = null;
        if (path != null) {
            try {
                ImageVo vo = RestClient.get(path, ImageVo.class, null, new Locale("es"));
                if (vo != null && vo.getImageBase64() != null && !vo.getImageBase64().isBlank()) {
                    bytes = Base64.getMimeDecoder().decode(vo.getImageBase64());
                }
            } catch (RestException | IllegalArgumentException e) {
                bytes = null;
            }
        }
        if (bytes == null || bytes.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(BusinessLogo.contentType(bytes));
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private static Integer parseId(String value) {
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
