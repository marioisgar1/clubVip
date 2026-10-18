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

    /** Payroll and commissions module (sueldos y comisiones del personal). */
    private Boolean modulePayroll;

    /** Consignment invitations waiting for this business to answer (as supplier). */
    private Integer consignmentPending;

    private List<String> permissionCodes = new ArrayList<>();

    private List<MenuItemVo> menu = new ArrayList<>();
    /** Contacts still to confirm with a code (EMAIL, PHONE, CONTACT_PHONE); empty = nothing pending. */
    private List<String> pendingVerifications = new ArrayList<>();

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

    public Boolean getModulePayroll() {
        return modulePayroll;
    }

    public void setModulePayroll(Boolean modulePayroll) {
        this.modulePayroll = modulePayroll;
    }

    public List<String> getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(List<String> pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    /** True while the owner still has an e-mail or phone to confirm. */
    public boolean isVerificationPending() {
        return pendingVerifications != null && !pendingVerifications.isEmpty();
    }

    /** "Confirmar después" in this session: the portal lets the owner in; the next login asks again. */
    private transient boolean verificationPostponed;

    public boolean isVerificationPostponed() {
        return verificationPostponed;
    }

    public void setVerificationPostponed(boolean verificationPostponed) {
        this.verificationPostponed = verificationPostponed;
    }

    /** The confirmation screen comes first: something pending and not postponed in this session. */
    public boolean isVerificationRequired() {
        return isVerificationPending() && !verificationPostponed;
    }

    /** Logged in with the temporary password from "forgot password": the new-password screen comes first. */
    private boolean mustChangePassword;
    /** The temporary password typed at login, needed as "current password" when choosing the new one. */
    private transient String temporaryPassword;

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    /** The business has not paid its membership (or it expired): only pages/membership.xhtml until it does. */
    private boolean membershipRequired;
    /** A renewal failed; the business keeps working until membershipGraceUntil (yyyy-MM-dd). */
    private boolean membershipPastDue;
    private String membershipGraceUntil;
    private String membershipStatus;
    /** COURTESY | MONTHLY | ANNUAL (null = none). */
    private String membershipPlan;
    /** Grace days left while membershipPastDue (0 = last day); -1 otherwise. */
    private int membershipDaysLeft = -1;

    /** El negocio dio de alta Rewards Club VIP (puntos) */
    private Boolean pointsEnabled;

    /** Rewards prendido y configurado (programa de lealtad activo) */
    private Boolean rewardsReady;

    public boolean isMembershipRequired() {
        return membershipRequired;
    }

    public void setMembershipRequired(boolean membershipRequired) {
        this.membershipRequired = membershipRequired;
    }

    public boolean isMembershipPastDue() {
        return membershipPastDue;
    }

    public void setMembershipPastDue(boolean membershipPastDue) {
        this.membershipPastDue = membershipPastDue;
    }

    public String getMembershipGraceUntil() {
        return membershipGraceUntil;
    }

    public void setMembershipGraceUntil(String membershipGraceUntil) {
        this.membershipGraceUntil = membershipGraceUntil;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public String getMembershipPlan() {
        return membershipPlan;
    }

    public void setMembershipPlan(String membershipPlan) {
        this.membershipPlan = membershipPlan;
    }

    public int getMembershipDaysLeft() {
        return membershipDaysLeft;
    }

    public void setMembershipDaysLeft(int membershipDaysLeft) {
        this.membershipDaysLeft = membershipDaysLeft;
    }

    public Boolean getPointsEnabled() {
        return pointsEnabled;
    }

    public void setPointsEnabled(Boolean pointsEnabled) {
        this.pointsEnabled = pointsEnabled;
    }

    public Boolean getRewardsReady() {
        return rewardsReady;
    }

    public void setRewardsReady(Boolean rewardsReady) {
        this.rewardsReady = rewardsReady;
    }
}
