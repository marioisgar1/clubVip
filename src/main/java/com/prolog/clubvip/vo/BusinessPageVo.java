package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * "Mi página" of a business: template, accent color, texts, optional address, social accounts,
 * section switches and the ids of its photos (cover + gallery, served by businessPage/getPhoto).
 */
public class BusinessPageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer businessId;

    private String template;

    private String accentColor;
    private String headline;
    private String subheadline;
    private String about;
    private String reason1Title;
    private String reason1Text;
    private String reason2Title;
    private String reason2Text;
    private String reason3Title;
    private String reason3Text;
    private String address;
    private String instagram;
    private String facebook;
    private String tiktok;

    private Boolean showServices;

    private Boolean showProducts;

    private Boolean showReviews;

    private Boolean published;

    private Date publishedAt;

    private Integer coverPhotoId;

    private List<Integer> galleryPhotoIds = new ArrayList<>();

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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSubheadline() {
        return subheadline;
    }

    public void setSubheadline(String subheadline) {
        this.subheadline = subheadline;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getReason1Title() {
        return reason1Title;
    }

    public void setReason1Title(String reason1Title) {
        this.reason1Title = reason1Title;
    }

    public String getReason1Text() {
        return reason1Text;
    }

    public void setReason1Text(String reason1Text) {
        this.reason1Text = reason1Text;
    }

    public String getReason2Title() {
        return reason2Title;
    }

    public void setReason2Title(String reason2Title) {
        this.reason2Title = reason2Title;
    }

    public String getReason2Text() {
        return reason2Text;
    }

    public void setReason2Text(String reason2Text) {
        this.reason2Text = reason2Text;
    }

    public String getReason3Title() {
        return reason3Title;
    }

    public void setReason3Title(String reason3Title) {
        this.reason3Title = reason3Title;
    }

    public String getReason3Text() {
        return reason3Text;
    }

    public void setReason3Text(String reason3Text) {
        this.reason3Text = reason3Text;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTiktok() {
        return tiktok;
    }

    public void setTiktok(String tiktok) {
        this.tiktok = tiktok;
    }

    public Boolean getShowServices() {
        return showServices;
    }

    public void setShowServices(Boolean showServices) {
        this.showServices = showServices;
    }

    public Boolean getShowProducts() {
        return showProducts;
    }

    public void setShowProducts(Boolean showProducts) {
        this.showProducts = showProducts;
    }

    public Boolean getShowReviews() {
        return showReviews;
    }

    public void setShowReviews(Boolean showReviews) {
        this.showReviews = showReviews;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getCoverPhotoId() {
        return coverPhotoId;
    }

    public void setCoverPhotoId(Integer coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    public List<Integer> getGalleryPhotoIds() {
        return galleryPhotoIds;
    }

    public void setGalleryPhotoIds(List<Integer> galleryPhotoIds) {
        this.galleryPhotoIds = galleryPhotoIds;
    }
}
