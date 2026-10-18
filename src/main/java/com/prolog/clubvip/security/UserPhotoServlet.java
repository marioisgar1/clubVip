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
 * Serves the logged user's own photo ({@code /userPhoto?u=userId&v=n}) for the menu avatar.
 * Private to the session: the URL names the user and is refused for anyone else, so a cached
 * URL never answers with another user's photo. 404 when there is no session or no photo.
 */
@WebServlet("/userPhoto")
public class UserPhotoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object u = session != null ? session.getAttribute(Constants.SESSION_USER) : null;
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        if (user == null || user.getUserId() == null
                || !String.valueOf(user.getUserId()).equals(request.getParameter("u"))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] photo = UserPhoto.of(session);
        if (photo.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(BusinessLogo.contentType(photo));
        response.setHeader("Cache-Control", "private, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(photo.length);
        response.getOutputStream().write(photo);
    }
}
