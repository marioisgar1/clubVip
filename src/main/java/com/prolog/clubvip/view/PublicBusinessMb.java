package com.prolog.clubvip.view;

import java.util.Map;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessDetailVo;
import com.prolog.clubvip.vo.BusinessSummaryVo;

import jakarta.faces.context.FacesContext;

/**
 * Shared part of the public pages (/b/{slug}/citas and /b/{slug}/tienda): the business behind
 * the slug, its header data and the links between sections and to the app. No login involved.
 */
public abstract class PublicBusinessMb extends BaseMb {

    private static final long serialVersionUID = 1L;

    protected String slug;
    protected BusinessDetailVo detail;

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
        return detail != null && detail.getBusiness() != null;
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

    public String getLogoUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getExternalContext().getRequestContextPath() + "/publicImage?logo=" + getBusiness().getId();
    }

    public boolean isHasLogo() {
        return isFound() && getBusiness().getLogoBase64() != null && !getBusiness().getLogoBase64().isBlank();
    }

    /** Section of the page: "citas" or "tienda". */
    public abstract String getSection();
}
