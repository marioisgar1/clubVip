package com.prolog.clubvip.security;

import java.io.IOException;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Serves the logged user's business logo ({@code /businessLogo?b=businessId&v=n}) for the branded topbar and
 * favicon. Private to the session; the {@code v} parameter changes on upload, so the browser can
 * keep it cached meanwhile. 404 when there is no session or the business has no logo.
 */
@WebServlet("/businessLogo")
public class BusinessLogoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object u = session != null ? session.getAttribute(Constants.SESSION_USER) : null;
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        // The URL names the business; refuse it unless it is the session's own business, so a URL
        // cached for one business never answers with another one's logo.
        if (user == null || user.getBusinessId() == null
                || !String.valueOf(user.getBusinessId()).equals(request.getParameter("b"))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] logo = BusinessLogo.of(session);
        if (logo.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(BusinessLogo.contentType(logo));
        response.setHeader("Cache-Control", "private, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(logo.length);
        response.getOutputStream().write(logo);
    }
}
