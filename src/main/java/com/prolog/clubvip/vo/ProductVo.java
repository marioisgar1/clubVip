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

    /** Optional; products with the same reference are one product with size / colour variants in the store. */
    private String referenceCode;

    private Integer familyId;

    private String familyName;

    private Integer brandId;

    private String brandName;

    private Integer modelId;

    private String modelName;
    /** Characteristics 1..4 and their names from the category (label null = not used); attributes = ready to show. */
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute1Label;
    private String attribute2Label;
    private String attribute3Label;
    private String attribute4Label;
    private java.util.List<AttributeVo> attributes = new java.util.ArrayList<>();


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

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
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

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }

    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }

    public String getAttribute1Label() {
        return attribute1Label;
    }

    public void setAttribute1Label(String attribute1Label) {
        this.attribute1Label = attribute1Label;
    }

    public String getAttribute2Label() {
        return attribute2Label;
    }

    public void setAttribute2Label(String attribute2Label) {
        this.attribute2Label = attribute2Label;
    }

    public String getAttribute3Label() {
        return attribute3Label;
    }

    public void setAttribute3Label(String attribute3Label) {
        this.attribute3Label = attribute3Label;
    }

    public String getAttribute4Label() {
        return attribute4Label;
    }

    public void setAttribute4Label(String attribute4Label) {
        this.attribute4Label = attribute4Label;
    }

    public java.util.List<AttributeVo> getAttributes() {
        return attributes;
    }

    public void setAttributes(java.util.List<AttributeVo> attributes) {
        this.attributes = attributes;
    }

    /** "Talla: M · Color: Negro" of the characteristics that are on and have a value. */
    public String getAttributesText() {
        StringBuilder sb = new StringBuilder();
        for (AttributeVo a : attributes != null ? attributes : java.util.Collections.<AttributeVo>emptyList()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(a.getLabel()).append(": ").append(a.getValue());
        }
        return sb.toString();
    }
}
