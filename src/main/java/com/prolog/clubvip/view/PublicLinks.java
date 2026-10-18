package com.prolog.clubvip.view;

import com.prolog.clubvip.constant.Constants;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Public links of a business ({@code /b/{slug}/citas}, {@code /b/{slug}/tienda}) and the Android
 * "open in the app" intent: the app when installed, else the Play Store.
 */
public final class PublicLinks {

    public static final String CITAS = "citas";
    public static final String TIENDA = "tienda";
    /** "Mi página": the landing page itself, served at /b/{slug} once published. */
    public static final String PAGINA = "pagina";

    private PublicLinks() {
    }

    /** Absolute public URL of a section of the business. */
    public static String link(String slug, String section) {
        return base() + "/b/" + slug + "/" + section;
    }

    /** Absolute URL of the landing page ("Mi página"): just /b/{slug}. */
    public static String pageLink(String slug) {
        return base() + "/b/" + slug;
    }

    /** Absolute URL of a path of this web app (images for link previews). */
    public static String absolute(String path) {
        return base() + path;
    }

    /**
     * The Club VIP app is not in the stores yet: while false, public pages hide every "open / get
     * the app" button. Switch to true when it is released.
     */
    public static final boolean APP_RELEASED = false;

    /**
     * {@code intent://host/path#Intent;scheme=https;package=...;S.browser_fallback_url=...;end}:
     * Chrome on Android opens the app on that section, or the Play Store when it is not installed.
     */
    public static String appIntent(String slug, String section) {
        String url = link(slug, section);
        String withoutScheme = url.replaceFirst("^https?://", "");
        String scheme = url.startsWith("http://") ? "http" : "https";
        return "intent://" + withoutScheme + "#Intent;scheme=" + scheme + ";package=" + Constants.APP_PACKAGE
                + ";S.browser_fallback_url=" + encode(Constants.PLAY_STORE_URL) + ";end";
    }

    private static String base() {
        if (Constants.PUBLIC_BASE_URL != null && !Constants.PUBLIC_BASE_URL.isBlank()) {
            return Constants.PUBLIC_BASE_URL.replaceAll("/+$", "");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return "";
        }
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest r = (HttpServletRequest) ec.getRequest();
        boolean defaultPort = ("http".equals(r.getScheme()) && r.getServerPort() == 80)
                || ("https".equals(r.getScheme()) && r.getServerPort() == 443);
        return r.getScheme() + "://" + r.getServerName() + (defaultPort ? "" : ":" + r.getServerPort())
                + r.getContextPath();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
