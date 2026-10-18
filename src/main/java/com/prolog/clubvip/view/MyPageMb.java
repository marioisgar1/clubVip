package com.prolog.clubvip.view;

import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BusinessPagePhotoVo;
import com.prolog.clubvip.vo.BusinessPageVo;
import com.prolog.clubvip.vo.BusinessVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * "Mi página" editor (pages/myPage.xhtml): template, free accent color, texts with suggestions
 * per business category, photos and publishing. The right side previews /b/{slug}/pagina?preview=1.
 * businessPage/getMyPage · saveUpdatePage · uploadPhoto · deletePhoto/{id}
 */
@Named("myPageMb")
@ViewScoped
public class MyPageMb extends BaseMb {

    private static final long serialVersionUID = 1L;
    public static final int GALLERY_LIMIT = 8;
    private static final String[] SUGGESTION_CODES = { "HEALTH", "BEAUTY", "PETS", "TRADES", "RETAIL" };

    private BusinessVo business = new BusinessVo();
    private BusinessPageVo page = new BusinessPageVo();
    private String coverBase64;
    private String galleryBase64;
    /** Bumped on every save so the preview iframe reloads. */
    private int previewVersion;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        try {
            business = RestClient.get("business/getMyBusiness", BusinessVo.class, userId(), locale());
            page = RestClient.get("businessPage/getMyPage", BusinessPageVo.class, userId(), locale());
            if (page.getId() == null) {
                // First visit: suggested texts for the category and the template's own color.
                fillSuggestions();
                page.setAccentColor(PageTheme.of(page.getTemplate()).getDefaultColor());
                page.setShowServices(Boolean.TRUE);
                page.setShowProducts(Boolean.TRUE);
                page.setShowReviews(Boolean.TRUE);
                page.setPublished(Boolean.FALSE);
            }
        } catch (RestException e) {
            errors(e);
        }
    }

    // ------------------------------------------------------------------ actions

    /** Saves quietly (called by the editor after every change); the preview reloads. */
    public void autoSave() {
        save(false);
    }

    public void saveNow() {
        save(true);
    }

    public void publish() {
        page.setPublished(Boolean.TRUE);
        if (save(false)) {
            info("pg.ed.published");
        } else {
            page.setPublished(Boolean.FALSE);
        }
    }

    public void unpublish() {
        page.setPublished(Boolean.FALSE);
        if (save(false)) {
            info("pg.ed.unpublished");
        }
    }

    private boolean save(boolean notify) {
        try {
            BusinessPageVo saved = RestClient.post("businessPage/saveUpdatePage", page, BusinessPageVo.class, userId(), locale());
            if (saved != null) {
                page = saved;
            }
            previewVersion++;
            if (notify) {
                info("pg.ed.saved");
            }
            return true;
        } catch (RestException e) {
            errors(e);
            return false;
        }
    }

    /** Replaces the texts with the suggestions for the business category. */
    public void suggest() {
        fillSuggestions();
        save(false);
        info("pg.ed.suggested");
    }

    private void fillSuggestions() {
        ResourceBundle b = ResourceBundle.getBundle("i18n.messages", locale());
        String code = "GENERIC";
        for (String c : SUGGESTION_CODES) {
            if (c.equals(business.getCategoryCode())) {
                code = c;
            }
        }
        String p = "pg.sg." + code + ".";
        page.setHeadline(text(b, p + "headline"));
        page.setSubheadline(text(b, p + "sub"));
        page.setAbout(text(b, p + "about").replace("{name}", business.getName() != null ? business.getName() : ""));
        page.setReason1Title(text(b, p + "r1t"));
        page.setReason1Text(text(b, p + "r1x"));
        page.setReason2Title(text(b, p + "r2t"));
        page.setReason2Text(text(b, p + "r2x"));
        page.setReason3Title(text(b, p + "r3t"));
        page.setReason3Text(text(b, p + "r3x"));
        if (page.getAddress() == null || page.getAddress().isBlank()) {
            page.setAddress(business.getAddress());
        }
    }

    private static String text(ResourceBundle b, String key) {
        try {
            return b.getString(key);
        } catch (MissingResourceException e) {
            return "";
        }
    }

    public void uploadCover() {
        upload("COVER", coverBase64);
        coverBase64 = null;
    }

    public void uploadGallery() {
        upload("GALLERY", galleryBase64);
        galleryBase64 = null;
    }

    private void upload(String kind, String base64) {
        if (base64 == null || base64.isBlank()) {
            errorKey("pg.ed.photo.none");
            return;
        }
        BusinessPagePhotoVo vo = new BusinessPagePhotoVo();
        vo.setKind(kind);
        vo.setImageBase64(base64);
        try {
            BusinessPagePhotoVo saved = RestClient.post("businessPage/uploadPhoto", vo, BusinessPagePhotoVo.class, userId(), locale());
            if ("COVER".equals(kind)) {
                page.setCoverPhotoId(saved.getId());
            } else {
                page.getGalleryPhotoIds().add(saved.getId());
            }
            previewVersion++;
        } catch (RestException e) {
            errors(e);
        }
    }

    public void deletePhoto(Integer photoId) {
        RestResponse r = RestClient.delete("businessPage/deletePhoto/" + photoId, userId(), locale());
        if (!r.isOk()) {
            error(String.join("\n", RestClient.extractErrors(r.getBody())));
            return;
        }
        if (photoId.equals(page.getCoverPhotoId())) {
            page.setCoverPhotoId(null);
        }
        page.getGalleryPhotoIds().remove(photoId);
        previewVersion++;
    }

    // ------------------------------------------------------------------ view helpers

    /** Lightest to darkest page, so the owner compares them side by side. */
    public List<PageTheme> getTemplates() {
        return PageTheme.BY_LIGHTNESS;
    }

    public PageTheme getTheme() {
        return PageTheme.of(page.getTemplate());
    }

    public boolean isHasSlug() {
        return business != null && business.getSlug() != null && !business.getSlug().isBlank();
    }

    public String getPageLink() {
        return isHasSlug() ? PublicLinks.pageLink(business.getSlug()) : null;
    }

    public String getPreviewUrl() {
        if (!isHasSlug()) {
            return null;
        }
        return contextPath() + "/b/" + business.getSlug() + "/pagina?preview=1&v=" + previewVersion;
    }

    public String photoUrl(Integer id) {
        return contextPath() + "/publicImage?pagePhoto=" + id;
    }

    public String getLogoUrl() {
        return business != null && business.getId() != null ? contextPath() + "/publicImage?logo=" + business.getId() : null;
    }

    public boolean isGalleryFull() {
        return page.getGalleryPhotoIds() != null && page.getGalleryPhotoIds().size() >= GALLERY_LIMIT;
    }

    public int getGalleryLimit() {
        return GALLERY_LIMIT;
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(page.getPublished());
    }

    private static String contextPath() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    // ------------------------------------------------------------------ getters / setters

    public BusinessVo getBusiness() {
        return business;
    }

    public BusinessPageVo getPage() {
        return page;
    }

    public String getCoverBase64() {
        return coverBase64;
    }

    public void setCoverBase64(String coverBase64) {
        this.coverBase64 = coverBase64;
    }

    public String getGalleryBase64() {
        return galleryBase64;
    }

    public void setGalleryBase64(String galleryBase64) {
        this.galleryBase64 = galleryBase64;
    }

    public int getPreviewVersion() {
        return previewVersion;
    }
}
