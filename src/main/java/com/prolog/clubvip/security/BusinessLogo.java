package com.prolog.clubvip.security;

import java.util.Base64;
import java.util.Locale;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ImageVo;
import com.prolog.clubvip.vo.LoginResponseVo;

import jakarta.servlet.http.HttpSession;

/**
 * Logo of the logged user's business, cached in the HTTP session so the portal can be branded
 * with it (topbar, favicon) without embedding the image in every page. Loaded once from
 * business/getLogo/{businessId}; an empty array means the business has no logo.
 */
public final class BusinessLogo {

    private static final String ATTR_BYTES = "businessLogo";
    private static final String ATTR_OWNER = "businessLogoOwner";
    private static final String ATTR_VERSION = "businessLogoVersion";
    private static final byte[] NONE = new byte[0];

    private BusinessLogo() {
    }

    public static byte[] of(HttpSession session) {
        if (session == null) {
            return NONE;
        }
        Object u = session.getAttribute(Constants.SESSION_USER);
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        if (user == null || user.getBusinessId() == null) {
            return NONE; // not cached: the user may still log in
        }
        // The cache belongs to one business: logging in as another user in the same session reloads it.
        Object cached = session.getAttribute(ATTR_BYTES);
        if (cached instanceof byte[] && user.getBusinessId().equals(session.getAttribute(ATTR_OWNER))) {
            return (byte[]) cached;
        }
        byte[] bytes = NONE;
        try {
            Locale locale = new Locale(user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "es");
            ImageVo vo = RestClient.get("business/getLogo/" + user.getBusinessId(), ImageVo.class,
                    user.getUserId(), locale);
            if (vo != null && vo.getImageBase64() != null && !vo.getImageBase64().isBlank()) {
                bytes = Base64.getMimeDecoder().decode(vo.getImageBase64());
            }
        } catch (RestException | IllegalArgumentException e) {
            bytes = NONE; // no branding rather than a broken page
        }
        session.setAttribute(ATTR_BYTES, bytes);
        session.setAttribute(ATTR_OWNER, user.getBusinessId());
        return bytes;
    }

    /** Version for cache busting the logo URL; bumped on every upload. */
    public static int version(HttpSession session) {
        Object v = session != null ? session.getAttribute(ATTR_VERSION) : null;
        return v instanceof Integer ? (Integer) v : 0;
    }

    /** Forgets the cached logo after it changes, so the next page loads the new one. */
    public static void invalidate(HttpSession session) {
        if (session != null) {
            session.removeAttribute(ATTR_BYTES);
            session.removeAttribute(ATTR_OWNER);
            session.setAttribute(ATTR_VERSION, version(session) + 1);
        }
    }

    /** Image media type sniffed from the first bytes (logos are stored without one). */
    public static String contentType(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length > 2 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if (b.length > 11 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        if (b.length > 2 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
            return "image/gif";
        }
        String head = new String(b, 0, Math.min(b.length, 256), java.nio.charset.StandardCharsets.UTF_8);
        if (head.contains("<svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }

    /**
     * "light" when the logo artwork is mostly light on a transparent background (white lettering,
     * pastel icons), so it is shown on an ink card; "dark" otherwise (white card). An opaque logo
     * brings its own background and gets "dark" too: the card never shows behind it.
     */
    public static String tone(byte[] bytes) {
        try {
            java.awt.image.BufferedImage img = bytes != null && bytes.length > 0
                    ? javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes)) : null;
            if (img == null || !img.getColorModel().hasAlpha()) {
                return "dark";
            }
            long opaque = 0;
            double lum = 0;
            int step = Math.max(1, img.getWidth() / 200);
            for (int y = 0; y < img.getHeight(); y += step) {
                for (int x = 0; x < img.getWidth(); x += step) {
                    int argb = img.getRGB(x, y);
                    if ((argb >>> 24) > 40) {
                        opaque++;
                        lum += (0.299 * ((argb >> 16) & 0xFF) + 0.587 * ((argb >> 8) & 0xFF) + 0.114 * (argb & 0xFF)) / 255;
                    }
                }
            }
            long sampled = (long) ((img.getHeight() + step - 1) / step) * ((img.getWidth() + step - 1) / step);
            boolean transparentBackground = opaque < sampled * 0.85;
            return transparentBackground && opaque > 0 && lum / opaque > 0.55 ? "light" : "dark";
        } catch (java.io.IOException | RuntimeException e) {
            return "dark";
        }
    }
}
