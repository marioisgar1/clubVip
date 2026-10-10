package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;

/**
 * Public business card shown in the directory and search results.
 */
public class BusinessSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** "Lun–Vie 9:00–18:00 · Sáb 8:00–16:00 · Dom cerrado". */
    private String hoursText;

    /** Hours per weekday (7 entries, ISO 1=Mon..7=Sun). */
    private List<DayHoursVo> weeklyHours;

    private Integer id;

    private String name;

    /** Short name of the public links (/b/{slug}/citas, /b/{slug}/tienda). */
    private String slug;

    private String categoryName;

    private String categoryCode;

    private String specialty;

    private String address;

    private String city;

    /** FIXED | MOBILE | ONLINE. */
    private String operationMode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean verified;

    private BigDecimal ratingAverage;

    private Integer ratingCount;

    private BigDecimal distanceKm;

    private String openingTime;

    private String closingTime;

    private String contactPhone;

    private String waLink;

    private String logoBase64;

    private Boolean moduleAppointments;

    private Boolean moduleSales;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(BigDecimal ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWaLink() {
        return waLink;
    }

    public void setWaLink(String waLink) {
        this.waLink = waLink;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getModuleAppointments() {
        return moduleAppointments;
    }

    public void setModuleAppointments(Boolean moduleAppointments) {
        this.moduleAppointments = moduleAppointments;
    }

    public Boolean getModuleSales() {
        return moduleSales;
    }

    public void setModuleSales(Boolean moduleSales) {
        this.moduleSales = moduleSales;
    }

    public List<DayHoursVo> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(List<DayHoursVo> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getHoursText() {
        return hoursText;
    }

    public void setHoursText(String hoursText) {
        this.hoursText = hoursText;
    }
}
