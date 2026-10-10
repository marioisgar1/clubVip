package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ProductVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Public store page (/b/{slug}/tienda): products with stock, sale price and description,
 * filtered by text, family, brand and model; buying is a WhatsApp message to the business.
 */
@Named("publicStoreMb")
@ViewScoped
public class PublicStoreMb extends PublicBusinessMb {

    private static final long serialVersionUID = 1L;
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final Type ID_LIST = new TypeToken<List<Integer>>() { }.getType();

    private List<ProductVo> products = new ArrayList<>();
    private String query;
    private String family;
    private String brand;
    private String model;
    /** Section of the store: {@link #LINE} (current products) or {@link #OUTLET}. */
    private String line = LINE;
    public static final String LINE = "line";
    public static final String OUTLET = "outlet";
    private ProductVo selected;
    /** Every photo of the selected product, primary first (gallery of the detail dialog). */
    private List<Integer> selectedPhotoIds = new ArrayList<>();
    /** The page came from a shared product link (?p=productId): open its detail on load. */
    private boolean openOnLoad;
    /** Cards shown; "Ver más" adds a page. */
    private int limit = PAGE;
    private static final int PAGE = 24;

    @PostConstruct
    public void init() {
        if (loadBusiness() && isHasStore()) {
            try {
                List<ProductVo> list = RestClient.get("product/getStore/" + getBusiness().getId(), PRODUCT_LIST, null, locale());
                products = list != null ? list : new ArrayList<>();
            } catch (RestException e) {
                products = new ArrayList<>();
            }
            openSharedProduct();
        }
    }

    /** {@code /b/{slug}/tienda?p=productId} (the link sent by WhatsApp) opens that product. */
    private void openSharedProduct() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = params.get("p");
        if (id == null || id.isBlank()) {
            return;
        }
        for (ProductVo p : products) {
            if (id.trim().equals(String.valueOf(p.getId()))) {
                select(p);
                openOnLoad = true;
                line = p.isOutlet() ? OUTLET : LINE; // the grid behind shows its section
                return;
            }
        }
    }

    @Override
    public String getSection() {
        return PublicLinks.TIENDA;
    }

    public List<ProductVo> getFiltered() {
        String q = normalize(query);
        List<ProductVo> result = new ArrayList<>();
        boolean split = isHasSections();
        for (ProductVo p : products) {
            if (split && p.isOutlet() != OUTLET.equals(line)) continue;
            if (family != null && !family.isEmpty() && !family.equals(p.getFamilyName())) continue;
            if (brand != null && !brand.isEmpty() && !brand.equals(p.getBrandName())) continue;
            if (model != null && !model.isEmpty() && !model.equals(p.getModelName())) continue;
            if (!q.isEmpty() && !normalize(p.getName() + " " + s(p.getBarcode()) + " " + s(p.getColor()) + " "
                    + s(p.getSize()) + " " + s(p.getShortDescription())).contains(q)) continue;
            result.add(p);
        }
        return result;
    }

    public List<ProductVo> getVisible() {
        List<ProductVo> all = getFiltered();
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    public boolean isHasMore() {
        return getFiltered().size() > limit;
    }

    public void showMore() {
        limit += PAGE;
    }

    // ---- Línea / Outlet: two sections only when the store has products of both kinds

    public boolean isHasSections() {
        return getOutletCount() > 0 && getOutletCount() < products.size();
    }

    public int getOutletCount() {
        int n = 0;
        for (ProductVo p : products) {
            if (p.isOutlet()) n++;
        }
        return n;
    }

    public int getLineCount() {
        return products.size() - getOutletCount();
    }

    public String getLine() {
        return line;
    }

    /** Switches section; filters stay, the page starts again. */
    public void showLine(String value) {
        line = OUTLET.equals(value) ? OUTLET : LINE;
        resetPage();
    }

    /** A new search or filter starts again from the first page. */
    public void resetPage() {
        limit = PAGE;
    }

    public List<String> getFamilies() {
        TreeSet<String> set = new TreeSet<>();
        for (ProductVo p : products) add(set, p.getFamilyName());
        return new ArrayList<>(set);
    }

    public List<String> getBrands() {
        TreeSet<String> set = new TreeSet<>();
        for (ProductVo p : products) add(set, p.getBrandName());
        return new ArrayList<>(set);
    }

    public List<String> getModels() {
        TreeSet<String> set = new TreeSet<>();
        for (ProductVo p : products) add(set, p.getModelName());
        return new ArrayList<>(set);
    }

    public void select(ProductVo p) {
        selected = p;
        selectedPhotoIds = new ArrayList<>();
        if (p == null) {
            return;
        }
        try {
            List<Integer> ids = RestClient.get("product/getStorePhotoIds/" + p.getId(), ID_LIST, null, locale());
            if (ids != null) {
                selectedPhotoIds = ids;
            }
        } catch (RestException e) {
            // the dialog falls back to the main photo
        }
    }

    public String photoUrl(ProductVo p) {
        return contextPath() + "/publicImage?photo=" + p.getId();
    }

    /** One photo of a store product, by the photo's id. */
    public String storePhotoUrl(Integer photoId) {
        return contextPath() + "/publicImage?storePhoto=" + photoId;
    }

    /** First image of the detail dialog: the primary photo of the gallery, or the main photo. */
    public String getSelectedMainPhotoUrl() {
        if (selected == null) {
            return null;
        }
        return selectedPhotoIds.isEmpty() ? photoUrl(selected) : storePhotoUrl(selectedPhotoIds.get(0));
    }

    public List<Integer> getSelectedPhotoIds() {
        return selectedPhotoIds;
    }

    public boolean isOpenOnLoad() {
        return openOnLoad;
    }

    /** Absolute link that opens the store on this product (its preview shows the product photo). */
    public String productLink(ProductVo p) {
        return PublicLinks.link(slug, PublicLinks.TIENDA) + "?p=" + p.getId();
    }

    // ---- link preview (Open Graph): the product when the link points to one, else the store

    public String getShareTitle() {
        if (selected != null && openOnLoad) {
            return selected.getName() + " · " + sessionMb.fmtMoney(selected.getPrice());
        }
        return getBusiness().getName();
    }

    public String getShareDescription() {
        if (selected != null && openOnLoad) {
            StringBuilder d = new StringBuilder(s(selected.getShortDescription()));
            for (String line : specLines(selected)) {
                d.append(d.length() > 0 ? " · " : "").append(line);
            }
            return d.toString();
        }
        return detail != null && detail.getDescription() != null ? detail.getDescription() : s(getBusiness().getSpecialty());
    }

    public String getShareUrl() {
        return selected != null && openOnLoad ? productLink(selected) : PublicLinks.link(slug, PublicLinks.TIENDA);
    }

    public String getShareImage() {
        if (selected != null && openOnLoad) {
            return PublicLinks.absolute("/publicImage?photo=" + selected.getId());
        }
        return isHasLogo() ? PublicLinks.absolute("/publicImage?logo=" + getBusiness().getId()) : null;
    }

    /** "Familia: X", "Marca: Y"… in the order of the detail dialog, only the ones with a value. */
    private List<String> specLines(ProductVo p) {
        List<String> lines = new ArrayList<>();
        addSpec(lines, "pub.store.family", p.getFamilyName());
        addSpec(lines, "pub.store.brand", p.getBrandName());
        addSpec(lines, "pub.store.model", p.getModelName());
        addSpec(lines, "pub.store.size", p.getSize());
        addSpec(lines, "pub.store.color", p.getColor());
        addSpec(lines, "pub.store.code", p.getBarcode());
        return lines;
    }

    private void addSpec(List<String> lines, String key, String value) {
        if (value != null && !value.isBlank()) {
            lines.add(sessionMb.text(key) + ": " + value.trim());
        }
    }

    private static String contextPath() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    /** WhatsApp to the business with the product prefilled. */
    public String whatsApp(ProductVo p) {
        if (p == null || getBusiness().getWaLink() == null) {
            return null;
        }
        // Same content as the detail dialog. WhatsApp cannot attach a photo from a link, but the
        // product link at the end makes it show a preview card with the product photo.
        StringBuilder t = new StringBuilder(sessionMb.text("pub.store.wa.intro").replace("{business}", getBusiness().getName()))
                .append("\n\n*").append(p.getName().trim()).append('*')
                .append(p.isOutlet() ? " · " + sessionMb.text("pub.store.outlet") : "")
                .append("\n*").append(sessionMb.text("pub.store.price")).append(": ").append(sessionMb.fmtMoney(p.getPrice())).append('*');
        if (p.getShortDescription() != null && !p.getShortDescription().isBlank()) {
            t.append("\n_").append(p.getShortDescription().trim()).append('_');
        }
        List<String> specs = specLines(p);
        if (!specs.isEmpty()) {
            t.append('\n');
            for (String line : specs) {
                t.append("\n• ").append(line);
            }
        }
        t.append("\n\n").append(productLink(p));
        t.append("\n\n").append(sessionMb.text("pub.store.wa.question"));
        return WhatsAppLink.withText(getBusiness().getWaLink(), t.toString());
    }

    private static void add(TreeSet<String> set, String v) {
        if (v != null && !v.trim().isEmpty()) set.add(v);
    }

    private static String s(String v) {
        return v != null ? v : "";
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text != null ? text : "", Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public int getTotal() {
        return products.size();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ProductVo getSelected() {
        return selected;
    }
}
