package com.prolog.clubvip.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Login result: user data, business/customer/employee context and the permission/menu tree.
 */
public class LoginResponseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    private String username;

    private String fullName;

    private String email;

    private String phone;

    private Integer profileId;

    private String profileName;

    private String preferredLanguage;

    private String preferredTheme;

    private Integer businessId;

    private String businessName;

    private Boolean businessVerified;

    private String verificationStatus;

    private Integer customerId;

    private Integer employeeId;

    /** Business module switches (true when the user has no business). */
    private Boolean moduleAppointments;

    private Boolean moduleSales;

    private Boolean moduleConsignment;

    /** Consignment invitations waiting for this business to answer (as supplier). */
    private Integer consignmentPending;

    private List<String> permissionCodes = new ArrayList<>();

    private List<MenuItemVo> menu = new ArrayList<>();

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPreferredTheme() {
        return preferredTheme;
    }

    public void setPreferredTheme(String preferredTheme) {
        this.preferredTheme = preferredTheme;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Boolean getBusinessVerified() {
        return businessVerified;
    }

    public void setBusinessVerified(Boolean businessVerified) {
        this.businessVerified = businessVerified;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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

    public Integer getConsignmentPending() {
        return consignmentPending;
    }

    public void setConsignmentPending(Integer consignmentPending) {
        this.consignmentPending = consignmentPending;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public List<MenuItemVo> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuItemVo> menu) {
        this.menu = menu;
    }
}
