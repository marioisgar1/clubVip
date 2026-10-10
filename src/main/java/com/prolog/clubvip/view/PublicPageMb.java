package com.prolog.clubvip.view;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.BusinessPageVo;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.ProductVo;
import com.prolog.clubvip.vo.ReviewVo;
import com.prolog.clubvip.vo.ServiceVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * "Mi página": the landing page of a business at /b/{slug} (public/pagina.xhtml). Shows the
 * published page; with {@code ?preview=1} the owner of the business sees the draft (editor
 * preview). When there is nothing to show it sends the visitor to the booking / store page.
 */
@Named("publicPageMb")
@ViewScoped
public class PublicPageMb extends PublicBusinessMb {

    private static final long serialVersionUID = 1L;
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final int SERVICES_SHOWN = 6;
    private static final int PRODUCTS_SHOWN = 6;
    private static final int REVIEWS_SHOWN = 3;

    private BusinessPageVo page;
    private PageTheme theme;
    private boolean preview;
    private List<ProductVo> products = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (!loadBusiness()) {
            return;
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        LoginResponseVo user = sessionMb.getUser();
        preview = "1".equals(params.get("preview")) && user != null && user.getBusinessId() != null
                && user.getBusinessId().equals(getBusiness().getId());
        try {
            page = preview
                    ? RestClient.get("businessPage/getMyPage", BusinessPageVo.class, user.getUserId(), locale())
                    : RestClient.get("businessPage/getPublicPage/" + RestClient.encode(slug), BusinessPageVo.class, null, locale());
        } catch (RestException e) {
            page = null;
        }
        if (page == null) {
            redirect(isHasAppointments() ? getCitasLink() : getTiendaLink());
            return;
        }
        theme = PageTheme.of(page.getTemplate());
        if (isHasStore() && !Boolean.FALSE.equals(page.getShowProducts())) {
            try {
                List<ProductVo> list = RestClient.get("product/getStore/" + getBusiness().getId(), PRODUCT_LIST, null, locale());
                if (list != null) {
                    products = list.size() > PRODUCTS_SHOWN ? new ArrayList<>(list.subList(0, PRODUCTS_SHOWN)) : list;
                }
            } catch (RestException e) {
                products = new ArrayList<>();
            }
        }
    }

    private void redirect(String url) {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(url);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            // nothing else to do: the page renders empty
        }
    }

    @Override
    public String getSection() {
        return PublicLinks.PAGINA;
    }

    public boolean isReady() {
        return isFound() && page != null;
    }

    public BusinessPageVo getPage() {
        return page;
    }

    public boolean isPreview() {
        return preview;
    }

    // ------------------------------------------------------------------ look

    public PageTheme getTheme() {
        return theme != null ? theme : PageTheme.of(null);
    }

    public String getBodyClass() {
        return "pg " + getTheme().getCssClass() + (getTheme().isDark() ? " pg-dark" : "") + (hasCover() ? " pg-has-cover" : "");
    }

    public String getColorVars() {
        return getTheme().colorVars(page != null ? page.getAccentColor() : null);
    }

    // ------------------------------------------------------------------ texts

    public String getHeadline() {
        return firstNonBlank(page.getHeadline(), getBusiness().getName());
    }

    public String getSubheadline() {
        return firstNonBlank(page.getSubheadline(), detail != null ? detail.getDescription() : null);
    }

    /** "Salón de belleza · Roma Norte" style line above the headline. */
    public String getKicker() {
        List<String> parts = new ArrayList<>();
        String what = firstNonBlank(getBusiness().getSpecialty(), getBusiness().getCategoryName());
        if (what != null) {
            parts.add(what);
        }
        if (getBusiness().getCity() != null && !getBusiness().getCity().isBlank()) {
            parts.add(getBusiness().getCity().trim());
        }
        return String.join(" · ", parts);
    }

    public String getInitials() {
        return sessionMb.initialsOf(getBusiness().getName());
    }

    public boolean isHasAbout() {
        return notBlank(page.getAbout());
    }

    /** Reasons with a title, as [title, text] pairs. */
    public List<String[]> getReasons() {
        List<String[]> list = new ArrayList<>();
        addReason(list, page.getReason1Title(), page.getReason1Text());
        addReason(list, page.getReason2Title(), page.getReason2Text());
        addReason(list, page.getReason3Title(), page.getReason3Text());
        return list;
    }

    private static void addReason(List<String[]> list, String title, String text) {
        if (notBlank(title)) {
            list.add(new String[] { title.trim(), text != null ? text.trim() : "" });
        }
    }

    // ------------------------------------------------------------------ catalog

    public List<ServiceVo> getServices() {
        if (!isHasAppointments() || Boolean.FALSE.equals(page.getShowServices()) || detail == null || detail.getServices() == null) {
            return Collections.emptyList();
        }
        List<ServiceVo> list = new ArrayList<>();
        for (ServiceVo s : detail.getServices()) {
            if (!Boolean.FALSE.equals(s.getActiveForBooking())) {
                list.add(s);
            }
            if (list.size() == SERVICES_SHOWN) {
                break;
            }
        }
        return list;
    }

    public List<ProductVo> getProducts() {
        return products;
    }

    public String productPhotoUrl(ProductVo p) {
        return contextPath() + "/publicImage?photo=" + p.getId();
    }

    public List<ReviewVo> getReviews() {
        if (Boolean.FALSE.equals(page.getShowReviews()) || detail == null || detail.getReviews() == null) {
            return Collections.emptyList();
        }
        List<ReviewVo> list = new ArrayList<>();
        for (ReviewVo r : detail.getReviews()) {
            if (notBlank(r.getComment())) {
                list.add(r);
            }
            if (list.size() == REVIEWS_SHOWN) {
                break;
            }
        }
        return list;
    }

    public String stars(Integer rating) {
        int n = rating != null ? Math.max(0, Math.min(5, rating)) : 0;
        return "★★★★★".substring(0, n) + "☆☆☆☆☆".substring(0, 5 - n);
    }

    public boolean isHasRating() {
        return getBusiness().getRatingCount() != null && getBusiness().getRatingCount() > 0;
    }

    public String getRatingText() {
        BigDecimal avg = getBusiness().getRatingAverage();
        return avg != null ? avg.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() : "";
    }

    // ------------------------------------------------------------------ photos

    public boolean hasCover() {
        return page != null && page.getCoverPhotoId() != null;
    }

    public boolean isHasCover() {
        return hasCover();
    }

    public String getCoverUrl() {
        return hasCover() ? contextPath() + "/publicImage?pagePhoto=" + page.getCoverPhotoId() : null;
    }

    public List<String> getGalleryUrls() {
        List<String> urls = new ArrayList<>();
        if (page != null && page.getGalleryPhotoIds() != null) {
            for (Integer id : page.getGalleryPhotoIds()) {
                urls.add(contextPath() + "/publicImage?pagePhoto=" + id);
            }
        }
        return urls;
    }

    /** Image for link previews in WhatsApp / Facebook: the cover, else the logo. */
    public String getShareImage() {
        if (hasCover()) {
            return PublicLinks.absolute("/publicImage?pagePhoto=" + page.getCoverPhotoId());
        }
        return isHasLogo() ? PublicLinks.absolute("/publicImage?logo=" + getBusiness().getId()) : null;
    }

    public String getShareUrl() {
        return PublicLinks.pageLink(slug);
    }

    // ------------------------------------------------------------------ actions

    public String getWaLink() {
        return getBusiness().getWaLink();
    }

    public boolean isHasWhatsapp() {
        return notBlank(getWaLink());
    }

    /** Main button: book when the business takes appointments, else the store, else WhatsApp. */
    public String getPrimaryLink() {
        return isHasAppointments() ? getCitasLink() : isHasStore() ? getTiendaLink() : getWaLink();
    }

    public String getPrimaryKey() {
        return isHasAppointments() ? "pg.cta.book" : isHasStore() ? "pg.cta.store" : "pg.cta.whatsapp";
    }

    public boolean isHasAddress() {
        return notBlank(page.getAddress());
    }

    public String getMapsLink() {
        String q = page.getAddress().trim();
        if (getBusiness().getCity() != null && !q.toLowerCase().contains(getBusiness().getCity().trim().toLowerCase())) {
            q = q + ", " + getBusiness().getCity().trim();
        }
        return "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
    }

    public String getHoursText() {
        return getBusiness().getHoursText();
    }

    public String getInstagramLink() {
        return notBlank(page.getInstagram()) ? "https://instagram.com/" + page.getInstagram() : null;
    }

    public String getTiktokLink() {
        return notBlank(page.getTiktok()) ? "https://www.tiktok.com/@" + page.getTiktok() : null;
    }

    public String getFacebookLink() {
        if (!notBlank(page.getFacebook())) {
            return null;
        }
        String f = page.getFacebook().trim();
        return f.startsWith("http") ? f : "https://facebook.com/" + f.replaceFirst("^@", "");
    }

    public boolean isHasSocial() {
        return getInstagramLink() != null || getTiktokLink() != null || getFacebookLink() != null;
    }

    // ------------------------------------------------------------------ helpers

    private static String contextPath() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String firstNonBlank(String a, String b) {
        return notBlank(a) ? a.trim() : notBlank(b) ? b.trim() : null;
    }
}
