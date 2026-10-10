package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product sold by a business, with its photos and stock snapshot. {@code name} maps to the
 * {@code description} column and {@code price} to {@code retail_price}; the Leonisa fields
 * (barcode, family, brand, model, size, color) are exposed alongside.
 */
public class ProductVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer businessId;

    private String name;

    private String shortDescription;

    private String barcode;

    private Integer familyId;

    private String familyName;

    private Integer brandId;

    private String brandName;

    private Integer modelId;

    private String modelName;

    private String size;

    private String color;

    /** Retail price (column retail_price). */
    private BigDecimal price;

    private Integer minStock;

    private Boolean availableForBooking;

    /** Shown in the public online store (when it has stock). */
    private Boolean availableInStore;

    /** Outlet (discontinued / clearance) instead of the current line. */
    private Boolean isOutlet;

    private Integer quantityOnHand;

    private BigDecimal averageCost;

    private List<ProductPhotoVo> photos = new ArrayList<>();

    private String primaryPhotoBase64;

    private Integer status;

    /** Set when the product is a store copy received on consignment (read-only for the store). */
    private Integer consignmentAgreementId;

    private String consignmentSupplierName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Boolean getAvailableForBooking() {
        return availableForBooking;
    }

    public void setAvailableForBooking(Boolean availableForBooking) {
        this.availableForBooking = availableForBooking;
    }

    public Boolean getAvailableInStore() {
        return availableInStore;
    }

    public void setAvailableInStore(Boolean availableInStore) {
        this.availableInStore = availableInStore;
    }

    public Boolean getIsOutlet() {
        return isOutlet;
    }

    public void setIsOutlet(Boolean isOutlet) {
        this.isOutlet = isOutlet;
    }

    /** For EL: outlet only when explicitly flagged. */
    public boolean isOutlet() {
        return Boolean.TRUE.equals(isOutlet);
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public List<ProductPhotoVo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<ProductPhotoVo> photos) {
        this.photos = photos;
    }

    public String getPrimaryPhotoBase64() {
        return primaryPhotoBase64;
    }

    public void setPrimaryPhotoBase64(String primaryPhotoBase64) {
        this.primaryPhotoBase64 = primaryPhotoBase64;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getConsignmentAgreementId() {
        return consignmentAgreementId;
    }

    public void setConsignmentAgreementId(Integer consignmentAgreementId) {
        this.consignmentAgreementId = consignmentAgreementId;
    }

    public String getConsignmentSupplierName() {
        return consignmentSupplierName;
    }

    public void setConsignmentSupplierName(String consignmentSupplierName) {
        this.consignmentSupplierName = consignmentSupplierName;
    }
}
