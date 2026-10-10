package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Business of the network: full data for the owner/admin. {@code bankAccount} is input only; {@code bankAccountMasked} is returned.
 */
public class BusinessVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Hours per weekday (7 entries, ISO 1=Mon..7=Sun). */
    private List<DayHoursVo> weeklyHours;

    private Integer id;

    private String name;

    /** Short name of the public links (/b/{slug}/citas, /b/{slug}/tienda). */
    private String slug;

    private String ownerName;

    private Integer categoryId;

    private String categoryName;

    private String categoryCode;

    private Boolean requiresLicense;

    private String specialty;

    private String description;

    private String address;

    private String city;

    /** FIXED | MOBILE | ONLINE. */
    private String operationMode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String openingTime;

    private String closingTime;

    private String closedWeekdays;

    private String breakStartTime;

    private String breakEndTime;

    private String contactPhone;

    private String groupPhone;

    private String bankName;

    private String bankAccountMasked;

    private String bankAccount;

    private Boolean onlinePaymentEnabled;

    private String autoReplyMessage;

    private Integer inactiveMonths;

    private Integer bookingHorizonDays;

    private Integer slotMinutes;

    private Boolean moduleAppointments;

    private Boolean moduleSales;

    private Boolean moduleConsignment;

    /** Rewards: points on purchases, and pesos per point. */
    private Boolean pointsEnabled;

    private BigDecimal pointsPesosPerPoint;

    private Integer exchangeMaxDays;

    private String verificationStatus;

    private String licenseFileName;

    private Boolean hasLicense;

    private Date verifiedAt;

    private String rejectionReason;

    private String subscriptionStatus;

    private String subscriptionUntil;

    private BigDecimal ratingAverage;

    private Integer ratingCount;

    private String logoBase64;

    private Integer status;

    private Date createdAt;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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

    public Boolean getRequiresLicense() {
        return requiresLicense;
    }

    public void setRequiresLicense(Boolean requiresLicense) {
        this.requiresLicense = requiresLicense;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getClosedWeekdays() {
        return closedWeekdays;
    }

    public void setClosedWeekdays(String closedWeekdays) {
        this.closedWeekdays = closedWeekdays;
    }

    public String getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(String breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public String getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(String breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getGroupPhone() {
        return groupPhone;
    }

    public void setGroupPhone(String groupPhone) {
        this.groupPhone = groupPhone;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountMasked() {
        return bankAccountMasked;
    }

    public void setBankAccountMasked(String bankAccountMasked) {
        this.bankAccountMasked = bankAccountMasked;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Boolean getOnlinePaymentEnabled() {
        return onlinePaymentEnabled;
    }

    public void setOnlinePaymentEnabled(Boolean onlinePaymentEnabled) {
        this.onlinePaymentEnabled = onlinePaymentEnabled;
    }

    public String getAutoReplyMessage() {
        return autoReplyMessage;
    }

    public void setAutoReplyMessage(String autoReplyMessage) {
        this.autoReplyMessage = autoReplyMessage;
    }

    public Integer getInactiveMonths() {
        return inactiveMonths;
    }

    public void setInactiveMonths(Integer inactiveMonths) {
        this.inactiveMonths = inactiveMonths;
    }

    public Integer getBookingHorizonDays() {
        return bookingHorizonDays;
    }

    public void setBookingHorizonDays(Integer bookingHorizonDays) {
        this.bookingHorizonDays = bookingHorizonDays;
    }

    public Integer getSlotMinutes() {
        return slotMinutes;
    }

    public void setSlotMinutes(Integer slotMinutes) {
        this.slotMinutes = slotMinutes;
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

    public Boolean getModuleConsignment() {
        return moduleConsignment;
    }

    public void setModuleConsignment(Boolean moduleConsignment) {
        this.moduleConsignment = moduleConsignment;
    }

    public Integer getExchangeMaxDays() {
        return exchangeMaxDays;
    }

    public void setExchangeMaxDays(Integer exchangeMaxDays) {
        this.exchangeMaxDays = exchangeMaxDays;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getLicenseFileName() {
        return licenseFileName;
    }

    public void setLicenseFileName(String licenseFileName) {
        this.licenseFileName = licenseFileName;
    }

    public Boolean getHasLicense() {
        return hasLicense;
    }

    public void setHasLicense(Boolean hasLicense) {
        this.hasLicense = hasLicense;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionUntil() {
        return subscriptionUntil;
    }

    public void setSubscriptionUntil(String subscriptionUntil) {
        this.subscriptionUntil = subscriptionUntil;
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

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public List<DayHoursVo> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(List<DayHoursVo> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Boolean getPointsEnabled() {
        return pointsEnabled;
    }

    public void setPointsEnabled(Boolean pointsEnabled) {
        this.pointsEnabled = pointsEnabled;
    }

    public BigDecimal getPointsPesosPerPoint() {
        return pointsPesosPerPoint;
    }

    public void setPointsPesosPerPoint(BigDecimal pointsPesosPerPoint) {
        this.pointsPesosPerPoint = pointsPesosPerPoint;
    }
}
