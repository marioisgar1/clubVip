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
 * Photo of the logged user (any profile), cached in the HTTP session so the menu avatar shows it
 * instead of the initials. Loaded once from user/getMyPhoto, which falls back to the user's
 * employee photo; an empty array means there is no photo (the avatar keeps the initials).
 */
public final class UserPhoto {

    private static final String ATTR_BYTES = "userPhoto";
    private static final String ATTR_OWNER = "userPhotoOwner";
    private static final String ATTR_VERSION = "userPhotoVersion";
    private static final byte[] NONE = new byte[0];

    private UserPhoto() {
    }

    public static byte[] of(HttpSession session) {
        if (session == null) {
            return NONE;
        }
        Object u = session.getAttribute(Constants.SESSION_USER);
        LoginResponseVo user = u instanceof LoginResponseVo ? (LoginResponseVo) u : null;
        if (user == null || user.getUserId() == null) {
            return NONE;
        }
        // The cache belongs to one user: another login in the same session reloads it.
        Object cached = session.getAttribute(ATTR_BYTES);
        if (cached instanceof byte[] && user.getUserId().equals(session.getAttribute(ATTR_OWNER))) {
            return (byte[]) cached;
        }
        byte[] bytes = NONE;
        try {
            Locale locale = new Locale(user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "es");
            ImageVo vo = RestClient.get("user/getMyPhoto", ImageVo.class, user.getUserId(), locale);
            if (vo != null && vo.getImageBase64() != null && !vo.getImageBase64().isBlank()) {
                bytes = Base64.getMimeDecoder().decode(vo.getImageBase64());
            }
        } catch (RestException | IllegalArgumentException e) {
            bytes = NONE; // initials rather than a broken page
        }
        session.setAttribute(ATTR_BYTES, bytes);
        session.setAttribute(ATTR_OWNER, user.getUserId());
        return bytes;
    }

    /** Version for cache busting the photo URL; bumped when the photo changes. */
    public static int version(HttpSession session) {
        Object v = session != null ? session.getAttribute(ATTR_VERSION) : null;
        return v instanceof Integer ? (Integer) v : 0;
    }

    /** Forgets the cached photo after it changes, so the next page loads the new one. */
    public static void invalidate(HttpSession session) {
        if (session != null) {
            session.removeAttribute(ATTR_BYTES);
            session.removeAttribute(ATTR_OWNER);
            session.setAttribute(ATTR_VERSION, version(session) + 1);
        }
    }
}
