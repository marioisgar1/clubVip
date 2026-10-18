package com.prolog.clubvip.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessDetailVo;
import com.prolog.clubvip.vo.BusinessSummaryVo;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.TrafficEventVo;
import com.prolog.clubvip.vo.TrafficVisitVo;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Shared part of the public pages (/b/{slug}/citas and /b/{slug}/tienda): the business behind
 * the slug, its header data and the links between sections and to the app. No login involved.
 */
public abstract class PublicBusinessMb extends BaseMb {

    private static final long serialVersionUID = 1L;

    /** Anonymous visitor cookie (one year) that tells new from returning visitors in the traffic report. */
    private static final String VISITOR_COOKIE = "cvv";

    protected String slug;
    protected BusinessDetailVo detail;
    /** Visit registered for the traffic report; null when this view is not counted (owner, preview, bot). */
    private Integer visitId;

    /** Loads the business of the {@code slug} request parameter; false when it does not exist. */
    protected boolean loadBusiness() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        slug = params.get("slug") != null ? params.get("slug").trim().toLowerCase() : null;
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        try {
            detail = RestClient.get("business/getBySlug/" + RestClient.encode(slug), BusinessDetailVo.class, null, locale());
        } catch (RestException e) {
            detail = null;
        }
        boolean found = detail != null && detail.getBusiness() != null;
        if (found) {
            loadPageTheme();
        }
        if (found && !"1".equals(params.get("preview"))) {
            openVisit();
        }
        return found;
    }

    // ------------------------------------------------------------------ look of "Mi página"

    /** Template and color of the business's published page ("Mi página"); null: the Club VIP lime look. */
    private PageTheme pageTheme;
    private String pageAccent;
    /** Cover photo of the published page; null when there is none (link previews fall back to the logo). */
    private Integer pageCoverPhotoId;

    /** The store and the booking page dress like the published page: same template fonts and color. */
    private void loadPageTheme() {
        try {
            com.prolog.clubvip.vo.BusinessPageVo page = RestClient.get("businessPage/getPublicPage/" + RestClient.encode(slug),
                    com.prolog.clubvip.vo.BusinessPageVo.class, null, locale());
            if (page != null) {
                pageTheme = PageTheme.of(page.getTemplate());
                pageAccent = page.getAccentColor();
                pageCoverPhotoId = page.getCoverPhotoId();
            }
        } catch (RestException e) {
            pageTheme = null; // no published page: keep the default look
        }
    }

    /**
     * Image for link previews in WhatsApp / Facebook: the cover of the published page, else the
     * logo, else none. Absolute URL (the previewer fetches it from outside).
     */
    public String getShareImage() {
        if (pageCoverPhotoId != null) {
            return PublicLinks.absolute("/publicImage?pagePhoto=" + pageCoverPhotoId);
        }
        return isHasLogo() ? PublicLinks.absolute("/publicImage?logo=" + getBusiness().getId()) : null;
    }

    public boolean isThemed() {
        return pageTheme != null;
    }

    /** Extra body classes: pub-themed pub-t-elegante (+ pub-dark on the dark templates). */
    public String getThemeClass() {
        return pageTheme == null ? "" : " pub-themed pub-t-" + pageTheme.getCode().toLowerCase(java.util.Locale.ROOT)
                + (pageTheme.isDark() ? " pub-dark" : "");
    }

    public String getThemeStyle() {
        return pageTheme == null ? "" : pageTheme.storeVars(pageAccent);
    }

    public String getThemeFontsUrl() {
        return pageTheme != null ? pageTheme.getFontsUrl() : null;
    }

    // ------------------------------------------------------------------ traffic report

    /**
     * Registers this page view as a visit of the business (traffic report). The owner looking at
     * their own pages is not a visitor; link previews and crawlers are filtered by the backend.
     */
    private void openVisit() {
        LoginResponseVo user = sessionMb.getUser();
        if (user != null && user.getBusinessId() != null && user.getBusinessId().equals(getBusiness().getId())) {
            return;
        }
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest req = (HttpServletRequest) ec.getRequest();
            TrafficVisitVo vo = new TrafficVisitVo();
            vo.setBusinessId(getBusiness().getId());
            vo.setVisitorKey(visitorKey(ec, req));
            vo.setUserId(user != null ? user.getUserId() : null);
            vo.setSource("WEB");
            vo.setSection(trafficSection());
            vo.setReferrer(req.getHeader("Referer"));
            vo.setUserAgent(req.getHeader("User-Agent"));
            TrafficVisitVo saved = RestClient.post("traffic/openVisit", vo, TrafficVisitVo.class, null, locale());
            visitId = saved != null ? saved.getVisitId() : null;
        } catch (RuntimeException e) {
            visitId = null; // the page never fails because of the statistics
        }
    }

    /** The visitor cookie, created on the first visit (same value for every business). */
    private static String visitorKey(ExternalContext ec, HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (VISITOR_COOKIE.equals(c.getName()) && c.getValue() != null && c.getValue().matches("[A-Za-z0-9-]{8,64}")) {
                    return c.getValue();
                }
            }
        }
        String key = UUID.randomUUID().toString();
        Map<String, Object> props = new HashMap<>();
        props.put("maxAge", 365 * 24 * 3600);
        props.put("path", ec.getRequestContextPath().isEmpty() ? "/" : ec.getRequestContextPath());
        props.put("httpOnly", true);
        props.put("secure", req.isSecure());
        ec.addResponseCookie(VISITOR_COOKIE, key, props);
        return key;
    }

    /** PAGE / STORE / BOOKING for the traffic report, from the page section. */
    private String trafficSection() {
        String section = getSection();
        if (PublicLinks.TIENDA.equals(section)) {
            return "STORE";
        }
        if (PublicLinks.CITAS.equals(section)) {
            return "BOOKING";
        }
        return "PAGE";
    }

    /** Records what happened in this visit (PRODUCT_VIEW, WA_CLICK, BOOKING); silent when there is no visit. */
    protected void track(String type, Integer productId, String detail) {
        if (visitId == null) {
            return;
        }
        try {
            RestClient.post("traffic/event", new TrafficEventVo(visitId, type, productId, detail), null, locale());
        } catch (RuntimeException e) {
            // statistics never break the page
        }
    }

    /** Visit id for the page beacons (time on page, WhatsApp clicks); null when the view is not counted. */
    public Integer getVisitId() {
        return visitId;
    }

    public boolean isFound() {
        return detail != null && detail.getBusiness() != null;
    }

    public BusinessSummaryVo getBusiness() {
        return detail != null ? detail.getBusiness() : null;
    }

    public BusinessDetailVo getDetail() {
        return detail;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isHasAppointments() {
        return isFound() && !Boolean.FALSE.equals(getBusiness().getModuleAppointments());
    }

    public boolean isHasStore() {
        return isFound() && Boolean.TRUE.equals(getBusiness().getModuleSales());
    }

    public String getCitasLink() {
        return PublicLinks.link(slug, PublicLinks.CITAS);
    }

    public String getTiendaLink() {
        return PublicLinks.link(slug, PublicLinks.TIENDA);
    }

    /** "Abrir en la app" for the current section. */
    public String getAppIntent() {
        return PublicLinks.appIntent(slug, getSection());
    }

    /** "Abrir en la app" buttons only once the app is in the stores ({@link PublicLinks#APP_RELEASED}). */
    public boolean isAppReleased() {
        return PublicLinks.APP_RELEASED;
    }

    public String getLogoUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getExternalContext().getRequestContextPath() + "/publicImage?logo=" + getBusiness().getId() + "&trim=1";
    }

    private String logoTone;

    /**
     * "light" when the logo artwork is mostly light on a transparent background (white lettering,
     * pastel icons), so the storefront shows it on an ink card; "dark" otherwise (white card). An
     * opaque logo brings its own background and gets "dark" too: the card never shows behind it.
     */
    public String getLogoTone() {
        if (logoTone == null) {
            logoTone = "dark";
            try {
                if (isHasLogo()) {
                    logoTone = com.prolog.clubvip.security.BusinessLogo.tone(
                            java.util.Base64.getMimeDecoder().decode(getBusiness().getLogoBase64()));
                }
            } catch (RuntimeException e) {
                logoTone = "dark";
            }
        }
        return logoTone;
    }

    public boolean isHasLogo() {
        return isFound() && getBusiness().getLogoBase64() != null && !getBusiness().getLogoBase64().isBlank();
    }

    /** Section of the page: "citas" or "tienda". */
    public abstract String getSection();
}
