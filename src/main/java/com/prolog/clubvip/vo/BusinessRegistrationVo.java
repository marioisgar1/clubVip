package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;


/**
 * Self-registration of an affiliate: business data, optional logo/license and the owner's login.
 */
public class BusinessRegistrationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Hours per weekday (7 entries, ISO 1=Mon..7=Sun). */
    private List<DayHoursVo> weeklyHours;

    private String name;

    private String ownerName;

    private Integer categoryId;

    private String specialty;

    private String address;

    private String city;

    /** FIXED | MOBILE | ONLINE. */
    private String operationMode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String openingTime;

    private String closingTime;

    private String closedWeekdays;

    private String contactPhone;

    private String groupPhone;

    private String bankName;

    private String bankAccount;

    private String logoBase64;

    private String licenseFileName;

    private String licenseContentType;

    private String licenseBase64;


    private String email;

    private String password;

    private Boolean privacyAccepted;

    /** Payment methods the business accepts (at least one active); commission = what the provider charges. */
    private List<PaymentMethodVo> paymentMethods;

    /** Where the business sells: branches, bazaars, online store... (at least one). */
    private List<SaleLocationVo> saleLocations;

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

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getLicenseFileName() {
        return licenseFileName;
    }

    public void setLicenseFileName(String licenseFileName) {
        this.licenseFileName = licenseFileName;
    }

    public String getLicenseContentType() {
        return licenseContentType;
    }

    public void setLicenseContentType(String licenseContentType) {
        this.licenseContentType = licenseContentType;
    }

    public String getLicenseBase64() {
        return licenseBase64;
    }

    public void setLicenseBase64(String licenseBase64) {
        this.licenseBase64 = licenseBase64;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getPrivacyAccepted() {
        return privacyAccepted;
    }

    public void setPrivacyAccepted(Boolean privacyAccepted) {
        this.privacyAccepted = privacyAccepted;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }

    public List<DayHoursVo> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(List<DayHoursVo> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public List<PaymentMethodVo> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethodVo> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public List<SaleLocationVo> getSaleLocations() {
        return saleLocations;
    }

    public void setSaleLocations(List<SaleLocationVo> saleLocations) {
        this.saleLocations = saleLocations;
    }
}
