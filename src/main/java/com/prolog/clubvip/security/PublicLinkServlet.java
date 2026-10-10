package com.prolog.clubvip.security;

import java.io.IOException;
import java.util.Locale;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessDetailVo;
import com.prolog.clubvip.vo.BusinessPageVo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Public links that businesses share on social media: {@code /b/{slug}/citas} and
 * {@code /b/{slug}/tienda}. When the Club VIP app is installed Android opens the app (App Links);
 * otherwise the browser lands here and gets the public page, no account needed.
 * {@code /b/{slug}} alone shows the business landing page ("Mi página") once published; until
 * then it picks citas, or tienda for a business that only sells. {@code /b/{slug}/pagina} is the
 * landing page too (the owner previews the draft there).
 */
@WebServlet("/b/*")
public class PublicLinkServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = request.getPathInfo() != null ? request.getPathInfo() : "";
        String[] parts = info.replaceAll("^/+|/+$", "").split("/");
        String slug = parts.length > 0 ? parts[0].trim().toLowerCase(Locale.ROOT) : "";
        if (!slug.matches("[a-z0-9-]{1,60}")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String section = parts.length > 1 ? parts[1].toLowerCase(Locale.ROOT) : "";
        if (section.isEmpty() && hasPublishedPage(slug)) {
            section = "pagina";
        }
        if (!"citas".equals(section) && !"tienda".equals(section) && !"pagina".equals(section)) {
            section = "citas";
            try {
                BusinessDetailVo d = RestClient.get("business/getBySlug/" + slug, BusinessDetailVo.class, null, new Locale("es"));
                if (d != null && d.getBusiness() != null && Boolean.FALSE.equals(d.getBusiness().getModuleAppointments())) {
                    section = "tienda";
                }
            } catch (RestException e) {
                // the page itself shows "business not found"
            }
        }
        request.getRequestDispatcher("/public/" + section + ".xhtml?slug=" + slug).forward(request, response);
    }

    private static boolean hasPublishedPage(String slug) {
        try {
            return RestClient.get("businessPage/getPublicPage/" + slug, BusinessPageVo.class, null, new Locale("es")) != null;
        } catch (RestException e) {
            return false;
        }
    }
}
