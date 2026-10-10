package com.prolog.clubvip.security;

import java.io.IOException;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.MenuItemVo;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Protects everything under /pages/*: requires a logged user and checks that the requested
 * view is allowed by the user's menu (outcomeWeb). Public pages (login, register, resources)
 * pass through.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // only guard protected pages
        if (!path.startsWith("/pages/")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        LoginResponseVo user = session != null
                ? (LoginResponseVo) session.getAttribute(Constants.SESSION_USER) : null;

        if (user == null) {
            response.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        // dashboard / home / accessDenied are always allowed; other pages must match an allowed outcomeWeb
        if (path.endsWith("/dashboard.xhtml") || path.endsWith("/home.xhtml")
                || path.endsWith("/accessDenied.xhtml") || isAllowed(user, path)) {
            chain.doFilter(req, res);
        } else {
            response.sendRedirect(contextPath + "/pages/accessDenied.xhtml");
        }
    }

    private boolean isAllowed(LoginResponseVo user, String path) {
        if (user.getMenu() == null) {
            return false;
        }
        for (MenuItemVo item : user.getMenu()) {
            if (item.getOutcomeWeb() != null && path.equals(item.getOutcomeWeb())) {
                return true;
            }
        }
        return false;
    }
}
