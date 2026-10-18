package com.prolog.clubvip.view;

import java.lang.reflect.Type;
import java.text.Normalizer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.AttributeVo;
import com.prolog.clubvip.vo.ProductVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
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
    /** Characteristic filters of the store: label ("Talla", "Sabor"…) -> chosen value. */
    private final Map<String, String> attrFilter = new LinkedHashMap<>();
    /** Price range chosen on the slider (null = whole range of the section). */
    private Integer priceMin;
    private Integer priceMax;
    /** relevance (catalog order) | priceAsc | priceDesc | nameAsc | newest */
    private String sort = "relevance";
    private static final int PRICE_STEP = 10;
    /** Section of the store: {@link #LINE} (current products) or {@link #OUTLET}. */
    private String line = LINE;
    public static final String LINE = "line";
    public static final String OUTLET = "outlet";
    private ProductVo selected;
    /** Card the detail dialog shows: a product alone, or every size / colour of a reference. */
    private StoreGroup selectedGroup;
    /** Characteristics of the variant shown in the dialog: label -> value. */
    private Map<String, String> selectedAttrs = new LinkedHashMap<>();
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
                line = p.isOutlet() ? OUTLET : LINE; // the grid behind shows its section
                selectedGroup = fullGroupOf(p);
                selectVariant(p);
                openOnLoad = true;
                track("PRODUCT_VIEW", p.getId(), "shared-link");
                return;
            }
        }
    }

    // ---- grouping by reference: one card per reference, its products are the size / colour variants

    /**
     * One card of the store. Products of the business with the same reference (each size / colour
     * has its own barcode) are one group; a product without reference is a group of one.
     */
    public static final class StoreGroup implements Serializable {
        private static final long serialVersionUID = 1L;
        private final List<ProductVo> variants = new ArrayList<>();

        public ProductVo getMain() {
            return variants.get(0);
        }

        public List<ProductVo> getVariants() {
            return variants;
        }

        public boolean isMulti() {
            return variants.size() > 1;
        }

        public String getReference() {
            return getMain().getReferenceCode();
        }

        /** Characteristic names of the products called {@code name} (null = all), in their order. */
        public List<String> labelsOf(String name) {
            List<String> out = new ArrayList<>();
            for (ProductVo v : variants) {
                if (name != null && !name.equals(nameKey(v))) continue;
                for (AttributeVo a : attrs(v)) {
                    if (!containsIgnoreCase(out, a.getLabel())) out.add(a.getLabel());
                }
            }
            return out;
        }

        /** Distinct values of one characteristic (S < M < L…, numbers by number, then alphabetical). */
        public List<String> valuesOf(String label, String name) {
            List<String> out = new ArrayList<>();
            for (ProductVo v : variants) {
                if (name != null && !name.equals(nameKey(v))) continue;
                String k = attrKey(v, label);
                if (!k.isEmpty() && !out.contains(k)) out.add(k);
            }
            out.sort(PublicStoreMb::compareSizes);
            return out;
        }

        public BigDecimal getMinPrice() {
            BigDecimal min = null;
            for (ProductVo v : variants) {
                if (v.getPrice() != null && (min == null || v.getPrice().compareTo(min) < 0)) min = v.getPrice();
            }
            return min != null ? min : BigDecimal.ZERO;
        }

        public boolean isSamePrice() {
            BigDecimal min = getMinPrice();
            for (ProductVo v : variants) {
                if (v.getPrice() != null && v.getPrice().compareTo(min) != 0) return false;
            }
            return true;
        }

        /** First variant called {@code name} (null = any) with every wanted characteristic value. */
        public ProductVo find(String name, Map<String, String> want) {
            for (ProductVo v : variants) {
                if (name != null && !name.equals(nameKey(v))) continue;
                boolean ok = true;
                for (Map.Entry<String, String> w : want.entrySet()) {
                    if (w.getValue() != null && !w.getValue().equals(attrKey(v, w.getKey()))) {
                        ok = false;
                        break;
                    }
                }
                if (ok) return v;
            }
            return null;
        }

        /** Grouped by model: the card is the model, its products are picked by name in the dialog. */
        public boolean isModel() {
            return getMain().getModelId() != null;
        }

        /** Distinct product names of the group, as written the first time ("Fanta" == "fanta"). */
        public List<String> getNames() {
            List<String> keys = new ArrayList<>();
            List<String> out = new ArrayList<>();
            for (ProductVo v : variants) {
                String k = nameKey(v);
                if (!keys.contains(k)) {
                    keys.add(k);
                    out.add(v.getName() != null ? v.getName().trim() : "");
                }
            }
            return out;
        }

        /** Several different products (not only sizes / colours of one): the dialog lets pick them. */
        public boolean isMultiName() {
            // only model groups: in a reference group the names are spellings of one product
            return isModel() && getNames().size() > 1;
        }

        /** Card title: the model when it groups different products, else the product itself. */
        public String getTitle() {
            String model = getMain().getModelName();
            return isModel() && isMultiName() && model != null && !model.trim().isEmpty() ? model.trim() : getMain().getName();
        }

    }

    /** "Fanta" / " fanta " are the same product name. */
    static String nameKey(ProductVo v) {
        return v.getName() != null ? v.getName().trim().toLowerCase(Locale.ROOT) : "";
    }

    private static final List<String> SIZE_ORDER = List.of(
            "XXS", "XS", "S", "M", "L", "XL", "XXL", "3XL", "XXXL", "4XL", "5XL");

    /** Letter sizes in their natural order, then numeric sizes by number, then alphabetical. */
    static int compareSizes(String a, String b) {
        int ra = SIZE_ORDER.indexOf(a), rb = SIZE_ORDER.indexOf(b);
        if (ra >= 0 || rb >= 0) {
            return ra >= 0 && rb >= 0 ? Integer.compare(ra, rb) : (ra >= 0 ? -1 : 1);
        }
        int na = leadingNumber(a), nb = leadingNumber(b);
        if (na >= 0 || nb >= 0) {
            if (na >= 0 && nb >= 0 && na != nb) return Integer.compare(na, nb);
            if (na < 0 || nb < 0) return na >= 0 ? -1 : 1;
        }
        return a.compareTo(b);
    }

    private static int leadingNumber(String s) {
        int i = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        return i == 0 ? -1 : Integer.parseInt(s.substring(0, Math.min(i, 9)));
    }

    static List<AttributeVo> attrs(ProductVo v) {
        return v.getAttributes() != null ? v.getAttributes() : java.util.Collections.emptyList();
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        for (String x : list) {
            if (x.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    /**
     * Value of a characteristic as one key: "xl" / "XL" are the same size and "negro" / "Negro" the
     * same colour — short values (sizes, 36B) upper case, longer ones with a capital first letter.
     */
    static String attrKey(ProductVo v, String label) {
        for (AttributeVo a : attrs(v)) {
            if (a.getLabel() != null && a.getLabel().equalsIgnoreCase(label)) {
                String c = a.getValue() != null ? a.getValue().trim() : "";
                if (c.isEmpty()) return "";
                if (c.length() <= 4) return c.toUpperCase(Locale.ROOT);
                return c.substring(0, 1).toUpperCase(Locale.ROOT) + c.substring(1).toLowerCase(Locale.ROOT);
            }
        }
        return "";
    }

    /** "talla" -> "tallas", "color" -> "colores", "presentación" -> "presentaciones" (card summary). */
    static String plural(String label) {
        String l = label.toLowerCase(Locale.ROOT);
        if (l.endsWith("ón")) return l.substring(0, l.length() - 2) + "ones";
        char last = l.charAt(l.length() - 1);
        return "aeiouáéíóú".indexOf(last) >= 0 ? l + "s" : l + "es";
    }

    private static List<StoreGroup> groupsOf(List<ProductVo> list) {
        Map<String, StoreGroup> byKey = new LinkedHashMap<>();
        for (ProductVo p : list) {
            // One card per model; without model, per reference number; else the product alone.
            String ref = p.getReferenceCode() != null ? p.getReferenceCode().trim().toUpperCase(Locale.ROOT) : "";
            String key = p.getModelId() != null ? "M:" + p.getModelId() : ref.isEmpty() ? "#" + p.getId() : "R:" + ref;
            StoreGroup g = byKey.get(key);
            if (g == null) {
                g = new StoreGroup();
                byKey.put(key, g);
            }
            g.variants.add(p);
        }
        return new ArrayList<>(byKey.values());
    }

    /** "3 tallas · 2 colores" for a reference card, else the size of the single product. */
    public String variantSummary(StoreGroup g) {
        if (!g.isMulti()) {
            List<AttributeVo> a = attrs(g.getMain());
            return a.isEmpty() ? "" : a.get(0).getValue();
        }
        if (g.isMultiName()) {
            return sessionMb.text("pub.store.products").replace("{0}", String.valueOf(g.getNames().size()));
        }
        // "4 tallas · 3 colores", "2 sabores"… with the names of the category's characteristics
        StringBuilder t = new StringBuilder();
        for (String label : g.labelsOf(null)) {
            int n = g.valuesOf(label, null).size();
            if (n == 0) continue;
            if (t.length() > 0) t.append(" · ");
            t.append(n).append(' ').append(n == 1 ? label.toLowerCase(Locale.ROOT) : plural(label));
        }
        return t.toString();
    }

    @Override
    public String getSection() {
        return PublicLinks.TIENDA;
    }

    /** Cards of the section that pass the search and every filter, in the chosen order. */
    public List<StoreGroup> getFiltered() {
        List<StoreGroup> groups = groupsOf(filteredProducts(null));
        switch (sort != null ? sort : "") {
            case "priceAsc":
                groups.sort((a, b) -> a.getMinPrice().compareTo(b.getMinPrice()));
                break;
            case "priceDesc":
                groups.sort((a, b) -> b.getMinPrice().compareTo(a.getMinPrice()));
                break;
            case "nameAsc":
                groups.sort((a, b) -> normalize(a.getMain().getName()).compareTo(normalize(b.getMain().getName())));
                break;
            case "newest":
                groups.sort((a, b) -> Integer.compare(newestId(b), newestId(a)));
                break;
            default:
                break; // catalog order
        }
        return groups;
    }

    private static int newestId(StoreGroup g) {
        int max = 0;
        for (ProductVo v : g.getVariants()) {
            if (v.getId() != null && v.getId() > max) max = v.getId();
        }
        return max;
    }

    /** Products of the current section that match the search (the base every filter works on). */
    private List<ProductVo> sectionProducts() {
        String q = normalize(query);
        List<ProductVo> result = new ArrayList<>();
        boolean split = isHasSections();
        for (ProductVo p : products) {
            if (split && p.isOutlet() != OUTLET.equals(line)) continue;
            if (!q.isEmpty() && !normalize(p.getName() + " " + s(p.getBarcode()) + " " + s(p.getReferenceCode()) + " "
                    + attrText(p) + " " + s(p.getShortDescription())).contains(q)) continue;
            result.add(p);
        }
        return result;
    }

    /**
     * Section products that pass every filter but {@code except} (null = all of them). Leaving one
     * filter out is how its own options are computed: a combo only offers values that still return
     * something together with the other filters.
     */
    private List<ProductVo> filteredProducts(String except) {
        List<ProductVo> result = new ArrayList<>();
        for (ProductVo p : sectionProducts()) {
            if (!"family".equals(except) && set(family) && !family.equals(p.getFamilyName())) continue;
            if (!"brand".equals(except) && set(brand) && !brand.equals(p.getBrandName())) continue;
            if (!"model".equals(except) && set(model) && !model.equals(p.getModelName())) continue;
            boolean attrsOk = true;
            for (Map.Entry<String, String> af : attrFilter.entrySet()) {
                if (("attr:" + af.getKey()).equals(except) || !set(af.getValue())) continue;
                if (!af.getValue().equals(attrKey(p, af.getKey()))) {
                    attrsOk = false;
                    break;
                }
            }
            if (!attrsOk) continue;
            if (!"price".equals(except) && !inPrice(p)) continue;
            result.add(p);
        }
        return result;
    }

    private static boolean set(String v) {
        return v != null && !v.isEmpty();
    }

    private boolean inPrice(ProductVo p) {
        if (priceMin == null && priceMax == null) return true;
        BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
        if (priceMin != null && price.compareTo(BigDecimal.valueOf(priceMin)) < 0) return false;
        return priceMax == null || price.compareTo(BigDecimal.valueOf(priceMax)) <= 0;
    }

    // ---- combo options: value + how many cards it would show, only values that return something

    private interface KeyOf {
        String of(ProductVo p);
    }

    private List<SelectItem> options(String except, KeyOf key, boolean sizes) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (StoreGroup g : groupsOf(filteredProducts(except))) {
            Set<String> seen = new java.util.HashSet<>();
            for (ProductVo v : g.getVariants()) {
                String k = key.of(v);
                if (k == null || k.trim().isEmpty() || !seen.add(k)) continue;
                counts.merge(k, 1, Integer::sum);
            }
        }
        List<String> keys = new ArrayList<>(counts.keySet());
        if (sizes) {
            keys.sort(PublicStoreMb::compareSizes);
        } else {
            keys.sort((a, b) -> normalize(a).compareTo(normalize(b)));
        }
        List<SelectItem> items = new ArrayList<>();
        for (String k : keys) {
            items.add(new SelectItem(k, k + " (" + counts.get(k) + ")"));
        }
        return items;
    }

    public List<SelectItem> getFamilyOptions() {
        return options("family", ProductVo::getFamilyName, false);
    }

    public List<SelectItem> getBrandOptions() {
        return options("brand", ProductVo::getBrandName, false);
    }

    public List<SelectItem> getModelOptions() {
        return options("model", ProductVo::getModelName, false);
    }

    /** One combo per characteristic the section's products have (Talla, Color, Sabor…). */
    public List<String> getFilterLabels() {
        List<String> out = new ArrayList<>();
        for (ProductVo p : sectionProducts()) {
            for (AttributeVo a : attrs(p)) {
                if (!containsIgnoreCase(out, a.getLabel())) out.add(a.getLabel());
            }
        }
        for (String l : attrFilter.keySet()) {
            if (set(attrFilter.get(l)) && !containsIgnoreCase(out, l)) out.add(l);
        }
        return out;
    }

    public List<SelectItem> attrOptions(String label) {
        return options("attr:" + label, p -> attrKey(p, label), true);
    }

    public Map<String, String> getAttrFilter() {
        return attrFilter;
    }

    private static String attrText(ProductVo p) {
        StringBuilder sb = new StringBuilder();
        for (AttributeVo a : attrs(p)) sb.append(a.getValue()).append(' ');
        return sb.toString();
    }

    // ---- price range of the section (slider bounds, rounded to the step)

    public int getPriceFloor() {
        BigDecimal min = null;
        for (ProductVo p : sectionProducts()) {
            if (p.getPrice() != null && (min == null || p.getPrice().compareTo(min) < 0)) min = p.getPrice();
        }
        return min == null ? 0 : (min.intValue() / PRICE_STEP) * PRICE_STEP;
    }

    public int getPriceCeil() {
        BigDecimal max = null;
        for (ProductVo p : sectionProducts()) {
            if (p.getPrice() != null && (max == null || p.getPrice().compareTo(max) > 0)) max = p.getPrice();
        }
        if (max == null) return 0;
        int ceil = ((max.intValue() + PRICE_STEP - 1) / PRICE_STEP) * PRICE_STEP;
        return Math.max(ceil, getPriceFloor() + PRICE_STEP);
    }

    public int getPriceStep() {
        return PRICE_STEP;
    }

    public boolean isHasPriceRange() {
        return getPriceCeil() - getPriceFloor() > PRICE_STEP;
    }

    /** Slider handles: the chosen limits, or the bounds while nothing is chosen. */
    public Integer getPriceMinValue() {
        return priceMin != null ? Math.max(priceMin, getPriceFloor()) : getPriceFloor();
    }

    public void setPriceMinValue(Integer v) {
        priceMin = v == null || v <= getPriceFloor() ? null : v;
    }

    public Integer getPriceMaxValue() {
        return priceMax != null ? Math.min(priceMax, getPriceCeil()) : getPriceCeil();
    }

    public void setPriceMaxValue(Integer v) {
        priceMax = v == null || v >= getPriceCeil() ? null : v;
    }

    public boolean isPriceFiltered() {
        return priceMin != null || priceMax != null;
    }

    public String getPriceLabel() {
        return sessionMb.fmtMoney(BigDecimal.valueOf(getPriceMinValue())) + " – "
                + sessionMb.fmtMoney(BigDecimal.valueOf(getPriceMaxValue()));
    }

    // ---- active filters as removable tags

    public static final class ActiveFilter implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String key;
        private final String label;

        ActiveFilter(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }

    public List<ActiveFilter> getActiveFilters() {
        List<ActiveFilter> list = new ArrayList<>();
        if (set(family)) list.add(new ActiveFilter("family", family));
        if (set(brand)) list.add(new ActiveFilter("brand", brand));
        if (set(model)) list.add(new ActiveFilter("model", model));
        for (Map.Entry<String, String> af : attrFilter.entrySet()) {
            if (set(af.getValue())) list.add(new ActiveFilter("attr:" + af.getKey(), af.getKey() + " " + af.getValue()));
        }
        if (isPriceFiltered()) list.add(new ActiveFilter("price", getPriceLabel()));
        return list;
    }

    public boolean isHasFilters() {
        return !getActiveFilters().isEmpty();
    }

    public void clearFilter(String key) {
        switch (key) {
            case "family": family = null; break;
            case "brand": brand = null; break;
            case "model": model = null; break;
            case "price": priceMin = null; priceMax = null; break;
            default:
                if (key.startsWith("attr:")) attrFilter.remove(key.substring(5));
                break;
        }
        resetPage();
    }

    public void clearFilters() {
        family = null;
        brand = null;
        model = null;
        attrFilter.clear();
        priceMin = null;
        priceMax = null;
        resetPage();
    }

    public List<StoreGroup> getVisible() {
        List<StoreGroup> all = getFiltered();
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
        return getOutletCount() > 0 && getLineCount() > 0;
    }

    /** Cards (references) of each section, so the counters match the grid. */
    public int getOutletCount() {
        return sectionGroups(true).size();
    }

    public int getLineCount() {
        return sectionGroups(false).size();
    }

    private List<StoreGroup> sectionGroups(boolean outlet) {
        List<ProductVo> list = new ArrayList<>();
        for (ProductVo p : products) {
            if (p.isOutlet() == outlet) list.add(p);
        }
        return groupsOf(list);
    }

    public String getLine() {
        return line;
    }

    /** Switches section; filters stay, the page starts again. */
    public void showLine(String value) {
        line = OUTLET.equals(value) ? OUTLET : LINE;
        priceMin = null; // the other section has its own price range
        priceMax = null;
        resetPage();
    }

    /** A new search or filter starts again from the first page. */
    public void resetPage() {
        limit = PAGE;
    }

    /**
     * Opens a card on its first variant that matches the filters, but with EVERY size / colour of
     * the reference available in the dialog (a size filter must not hide the other sizes there).
     */
    public void select(StoreGroup g) {
        if (g == null) {
            selectedGroup = null;
            selectVariant(null);
            return;
        }
        selectedGroup = fullGroupOf(g.getMain());
        selectVariant(g.getMain());
        track("PRODUCT_VIEW", g.getMain().getId(), null);
    }

    private StoreGroup fullGroupOf(ProductVo p) {
        for (StoreGroup g : sectionGroups(p.isOutlet())) {
            if (g.getVariants().contains(p)) return g;
        }
        StoreGroup single = new StoreGroup();
        single.variants.add(p);
        return single;
    }

    /** Product chip (model group): keeps the characteristics when that product has them, else its first variant. */
    public void pickName(String name) {
        if (selectedGroup == null || name == null) return;
        String key = name.trim().toLowerCase(Locale.ROOT);
        ProductVo v = selectedGroup.find(key, selectedAttrs);
        if (v == null) v = selectedGroup.find(key, new LinkedHashMap<>());
        if (v != null) selectVariant(v);
    }

    /** Name key of the product shown, so the characteristics are those of that product only. */
    private String selectedNameKey() {
        return selected != null && selectedGroup != null && selectedGroup.isMultiName() ? nameKey(selected) : null;
    }

    public boolean isNameActive(String name) {
        return selected != null && name != null && name.trim().toLowerCase(Locale.ROOT).equals(nameKey(selected));
    }

    /** Characteristic rows of the dialog (Talla, Color, Sabor…) of the product picked. */
    public List<String> getDialogLabels() {
        if (selectedGroup == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (String l : selectedGroup.labelsOf(selectedNameKey())) {
            if (!selectedGroup.valuesOf(l, selectedNameKey()).isEmpty()) out.add(l);
        }
        return out;
    }

    public List<String> dialogValues(String label) {
        return selectedGroup != null ? selectedGroup.valuesOf(label, selectedNameKey()) : new ArrayList<>();
    }

    /** Chip of a characteristic: keeps the others when that combination exists, else the first variant of the value. */
    public void pickAttr(String label, String value) {
        if (selectedGroup == null) return;
        Map<String, String> want = new LinkedHashMap<>(selectedAttrs);
        want.put(label, value);
        ProductVo v = selectedGroup.find(selectedNameKey(), want);
        if (v == null) {
            Map<String, String> only = new LinkedHashMap<>();
            only.put(label, value);
            v = selectedGroup.find(selectedNameKey(), only);
        }
        if (v != null) selectVariant(v);
    }

    /** A value that exists with the other characteristics chosen (chips of the others are dimmed, still clickable). */
    public boolean attrAvailable(String label, String value) {
        if (selectedGroup == null) return false;
        Map<String, String> want = new LinkedHashMap<>(selectedAttrs);
        want.put(label, value);
        return selectedGroup.find(selectedNameKey(), want) != null;
    }

    public boolean attrActive(String label, String value) {
        return value != null && value.equals(selectedAttrs.get(label));
    }

    public void selectVariant(ProductVo p) {
        selected = p;
        selectedPhotoIds = new ArrayList<>();
        if (p == null) {
            selectedAttrs = new LinkedHashMap<>();
            return;
        }
        selectedAttrs = new LinkedHashMap<>();
        for (AttributeVo a : attrs(p)) {
            String k = attrKey(p, a.getLabel());
            if (!k.isEmpty()) selectedAttrs.put(a.getLabel(), k);
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

    @Override
    public String getShareImage() {
        if (selected != null && openOnLoad) {
            return PublicLinks.absolute("/publicImage?photo=" + selected.getId());
        }
        return super.getShareImage();
    }

    /** "Familia: X", "Marca: Y"… in the order of the detail dialog, only the ones with a value. */
    private List<String> specLines(ProductVo p) {
        List<String> lines = new ArrayList<>();
        addSpec(lines, "pub.store.family", p.getFamilyName());
        addSpec(lines, "pub.store.brand", p.getBrandName());
        addSpec(lines, "pub.store.model", p.getModelName());
        addSpec(lines, "pub.store.reference", p.getReferenceCode());
        for (AttributeVo a : attrs(p)) {
            if (a.getValue() != null && !a.getValue().isBlank()) lines.add(a.getLabel() + ": " + a.getValue().trim());
        }
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



    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public ProductVo getSelected() {
        return selected;
    }

    public StoreGroup getSelectedGroup() {
        return selectedGroup;
    }


}
