package com.prolog.clubvip.view;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.rest.RestResponse;
import com.prolog.clubvip.vo.BrandVo;
import com.prolog.clubvip.vo.FamilyVo;
import com.prolog.clubvip.vo.ProductModelVo;
import com.prolog.clubvip.vo.ProductPhotoVo;
import com.prolog.clubvip.vo.ProductVo;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Product catalog (PRODUCT_VIEW): list without heavy images, edit with photos loaded on demand.
 * The POS fields (barcode, family, brand, model, size, color) sit next to the appointment ones.
 * product/getProducts?includeImages=false · product/getProduct/{id} · product/saveUpdateProduct · product/deleteProducts/{id}
 * family/getFamilies · brand/getBrands · productModel/getProductModels
 */
@Named("productMb")
@ViewScoped
public class ProductMb implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type PRODUCT_LIST = new TypeToken<List<ProductVo>>() { }.getType();
    private static final Type FAMILY_LIST = new TypeToken<List<FamilyVo>>() { }.getType();
    private static final Type BRAND_LIST = new TypeToken<List<BrandVo>>() { }.getType();
    private static final Type MODEL_LIST = new TypeToken<List<ProductModelVo>>() { }.getType();

    @Inject
    private SessionMb sessionMb;

    private List<ProductVo> products = new ArrayList<>();
    private List<ProductVo> filtered;
    private ProductVo selected = new ProductVo();
    private String newPhotoBase64;

    private List<FamilyVo> families = new ArrayList<>();
    private List<BrandVo> brands = new ArrayList<>();
    private List<ProductModelVo> models = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadCatalogs();
        refresh();
    }

    /** Family / brand / model selects of the edit dialog (each one fails silently: they are optional). */
    private void loadCatalogs() {
        try {
            List<FamilyVo> f = RestClient.get("family/getFamilies", FAMILY_LIST, userId(), sessionMb.getLocale());
            families = f != null ? f : new ArrayList<>();
        } catch (RestException e) {
            families = new ArrayList<>();
        }
        try {
            List<BrandVo> b = RestClient.get("brand/getBrands", BRAND_LIST, userId(), sessionMb.getLocale());
            brands = b != null ? b : new ArrayList<>();
        } catch (RestException e) {
            brands = new ArrayList<>();
        }
        try {
            List<ProductModelVo> m = RestClient.get("productModel/getProductModels", MODEL_LIST, userId(), sessionMb.getLocale());
            models = m != null ? m : new ArrayList<>();
        } catch (RestException e) {
            models = new ArrayList<>();
        }
    }

    public void refresh() {
        try {
            List<ProductVo> list = RestClient.get("product/getProducts?includeImages=false", PRODUCT_LIST,
                    userId(), sessionMb.getLocale());
            products = list != null ? list : new ArrayList<>();
        } catch (RestException e) {
            products = new ArrayList<>();
            errors(e.getMessages());
        }
    }

    /** Version in the thumbnail URLs; changes after a save so the browser reloads the photos. */
    public long getPhotoVersion() {
        return sessionMb.getProductPhotoVersion();
    }

    public void prepareNew() {
        selected = new ProductVo();
        selected.setAvailableForBooking(Boolean.TRUE);
        selected.setAvailableInStore(Boolean.TRUE);
        selected.setIsOutlet(Boolean.FALSE);
        selected.setMinStock(0);
        selected.setPhotos(new ArrayList<>());
        newPhotoBase64 = null;
    }

    /** Loads the full product (with photos) before editing. */
    public void prepareEdit(ProductVo vo) {
        newPhotoBase64 = null;
        try {
            ProductVo full = RestClient.get("product/getProduct/" + vo.getId(), ProductVo.class,
                    userId(), sessionMb.getLocale());
            selected = full != null ? full : vo;
        } catch (RestException e) {
            selected = vo;
            errors(e.getMessages());
        }
        if (selected.getPhotos() == null) {
            selected.setPhotos(new ArrayList<>());
        }
        if (selected.getAvailableInStore() == null) {
            // a backend without the flag: shown, as the store has always done
            selected.setAvailableInStore(Boolean.TRUE);
        }
        if (selected.getIsOutlet() == null) {
            selected.setIsOutlet(Boolean.FALSE);
        }
    }

    public void addPhoto() {
        if (newPhotoBase64 == null || newPhotoBase64.trim().isEmpty()) {
            return;
        }
        if (selected.getPhotos() == null) {
            selected.setPhotos(new ArrayList<>());
        }
        ProductPhotoVo photo = new ProductPhotoVo();
        photo.setImageBase64(stripDataUri(newPhotoBase64.trim()));
        photo.setSortOrder(selected.getPhotos().size() + 1);
        photo.setIsPrimary(selected.getPhotos().isEmpty());
        selected.getPhotos().add(photo);
        newPhotoBase64 = null;
    }

    public void removePhoto(ProductPhotoVo photo) {
        if (selected.getPhotos() != null) {
            selected.getPhotos().remove(photo);
            resequencePhotos();
        }
    }

    public void setPrimary(ProductPhotoVo photo) {
        if (selected.getPhotos() != null) {
            for (ProductPhotoVo p : selected.getPhotos()) {
                p.setIsPrimary(p == photo);
            }
        }
    }

    private void resequencePhotos() {
        List<ProductPhotoVo> photos = selected.getPhotos();
        if (photos == null) {
            return;
        }
        boolean anyPrimary = false;
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).setSortOrder(i + 1);
            if (Boolean.TRUE.equals(photos.get(i).getIsPrimary())) {
                anyPrimary = true;
            }
        }
        if (!anyPrimary && !photos.isEmpty()) {
            photos.get(0).setIsPrimary(true);
        }
    }

    /** Removes an optional {@code data:image/...;base64,} prefix so only the raw bytes travel. */
    static String stripDataUri(String value) {
        if (value == null) {
            return null;
        }
        int comma = value.indexOf(',');
        if (value.startsWith("data:") && comma >= 0) {
            return value.substring(comma + 1);
        }
        return value;
    }

    public void save() {
        RestResponse r = RestClient.post("product/saveUpdateProduct", selected, userId(), sessionMb.getLocale());
        if (r.isOk()) {
            sessionMb.bumpProductPhotoVersion(); // photos may have changed: new thumbnail URLs
            refresh();
            info(sessionMb.text("msg.saved"));
            PrimeFaces.current().executeScript("PF('productDialog').hide()");
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    public void delete(ProductVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        RestResponse r = RestClient.delete("product/deleteProducts/" + vo.getId(), userId(), sessionMb.getLocale());
        if (r.isOk()) {
            refresh();
            info(sessionMb.text("msg.deleted"));
        } else {
            errors(RestClient.extractErrors(r.getBody()));
        }
    }

    /** True when the on-hand quantity is at or below the minimum stock. */
    public boolean isLowStock(ProductVo vo) {
        if (vo == null) {
            return false;
        }
        int qty = vo.getQuantityOnHand() != null ? vo.getQuantityOnHand() : 0;
        int min = vo.getMinStock() != null ? vo.getMinStock() : 0;
        return qty <= min;
    }

    private Integer userId() {
        return sessionMb.getUserId();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void errors(List<String> messages) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return;
        }
        if (messages == null || messages.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, sessionMb.text("msg.error"), null));
            return;
        }
        for (String m : messages) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public List<ProductVo> getProducts() {
        return products;
    }

    public List<ProductVo> getFiltered() {
        return filtered;
    }

    public void setFiltered(List<ProductVo> filtered) {
        this.filtered = filtered;
    }

    public ProductVo getSelected() {
        return selected;
    }

    public void setSelected(ProductVo selected) {
        this.selected = selected;
    }

    public String getNewPhotoBase64() {
        return newPhotoBase64;
    }

    public List<FamilyVo> getFamilies() {
        return families;
    }

    public List<BrandVo> getBrands() {
        return brands;
    }

    public List<ProductModelVo> getModels() {
        return models;
    }

    public void setNewPhotoBase64(String newPhotoBase64) {
        this.newPhotoBase64 = newPhotoBase64;
    }
}
