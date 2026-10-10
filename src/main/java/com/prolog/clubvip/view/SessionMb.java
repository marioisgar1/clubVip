package com.prolog.clubvip.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.security.BusinessLogo;
import com.prolog.clubvip.vo.LoginResponseVo;
import com.prolog.clubvip.vo.MenuItemVo;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Session-wide state: logged user, dynamic menu, language (es/en) and theme (light/dark/dim),
 * plus small formatting helpers shared by every page bean.
 */
@Named("sessionMb")
@SessionScoped
public class SessionMb implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int PROFILE_OPERATOR = 1;
    public static final int PROFILE_ADMIN = 2;
    public static final int PROFILE_EMPLOYEE = 3;
    public static final int PROFILE_CUSTOMER = 4;
    public static final int PROFILE_SELLER = 5;

    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter UI_DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LoginResponseVo user;
    private String language = "es";
    private String theme = "light"; // light | dark | dim
    private MenuModel menuModel;
    /** Version of the product thumbnail URLs (/productPhoto?v=): bumped when a product is saved. */
    private long productPhotoVersion = System.currentTimeMillis();

    @PostConstruct
    public void init() {
        HttpSession session = currentSession(false);
        if (session != null) {
            Object u = session.getAttribute(Constants.SESSION_USER);
            if (u instanceof LoginResponseVo) {
                setUser((LoginResponseVo) u);
            }
        }
    }

    public void setUser(LoginResponseVo user) {
        this.user = user;
        if (user != null) {
            if (user.getPreferredLanguage() != null) {
                this.language = user.getPreferredLanguage();
            }
            if (user.getPreferredTheme() != null) {
                this.theme = user.getPreferredTheme();
            }
            buildMenu();
        }
    }

    public LoginResponseVo getUser() {
        return user;
    }

    /** Convenience for page beans: id of the acting user (X-Audit-User), null when not logged in. */
    public Integer getUserId() {
        return user != null ? user.getUserId() : null;
    }

    /**
     * Spanish is Mexican Spanish: plain "es" formats numbers the Spain way (4,50); es-MX uses the
     * decimal point (4.50). Texts still come from messages_es (bundle lookup falls back to it).
     */
    public Locale getLocale() {
        return "es".equals(language) ? new Locale("es", "MX") : new Locale(language);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        if (user != null) {
            buildMenu();
        }
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    /** Body CSS class that drives the light/dark/dim palette. */
    public String getThemeClass() {
        return "theme-" + (theme != null ? theme : "light");
    }

    /**
     * PrimeFaces component theme matching the selected palette.
     * saga (light) / vela (dim) / arya (dark) are the same design in 3 luminosities,
     * all bundled in the PrimeFaces jar. Referenced from web.xml's primefaces.THEME.
     */
    public String getPrimeTheme() {
        if ("dark".equals(theme)) return "arya";
        if ("dim".equals(theme))  return "vela";
        return "saga";
    }

    public void changeTheme(String theme) {
        this.theme = theme;
    }

    public void changeLanguage(String language) {
        setLanguage(language);
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getViewRoot() != null) {
            fc.getViewRoot().setLocale(getLocale());
        }
    }

    // ------------------------------------------------------------------ profile helpers

    public boolean hasPermission(String code) {
        return user != null && user.getPermissionCodes() != null
                && user.getPermissionCodes().contains(code);
    }

    private boolean hasProfile(int profileId) {
        return user != null && user.getProfileId() != null && user.getProfileId() == profileId;
    }

    /** CLUBVIP_OPERATOR (profile 1): Club VIP back office. */
    public boolean isOperator() {
        return hasProfile(PROFILE_OPERATOR);
    }

    /** BUSINESS_ADMIN (profile 2): owner / administrator of an affiliate. */
    public boolean isAdmin() {
        return hasProfile(PROFILE_ADMIN);
    }

    /** EMPLOYEE (profile 3): staff member with own agenda and commissions. */
    public boolean isEmployee() {
        return hasProfile(PROFILE_EMPLOYEE);
    }

    /** SELLER (profile 5): point-of-sale seller (sells, returns, exchanges, collects). */
    public boolean isSeller() {
        return hasProfile(PROFILE_SELLER);
    }

    /** True unless the business explicitly switched the appointments module off. */
    public boolean isAppointmentsEnabled() {
        return user == null || !Boolean.FALSE.equals(user.getModuleAppointments());
    }

    /** True unless the business explicitly switched the sales module off. */
    public boolean isSalesEnabled() {
        return user == null || !Boolean.FALSE.equals(user.getModuleSales());
    }

    /** The home page is the point of sale: sellers, and admins whose business has no appointments. */
    public boolean isSalesHome() {
        return isSeller() || (isAdmin() && !isAppointmentsEnabled());
    }

    public boolean isLogged() {
        return user != null;
    }

    public String getBusinessName() {
        return user != null && user.getBusinessName() != null ? user.getBusinessName() : "";
    }

    /** True when the logged user belongs to a business (admin, employee, seller). */
    public boolean isHasBusiness() {
        return user != null && user.getBusinessId() != null;
    }

    /** The business uploaded a logo: the portal is branded with it instead of Club VIP. */
    public boolean isHasBusinessLogo() {
        return isHasBusiness() && BusinessLogo.of(currentSession(false)).length > 0;
    }

    /**
     * URL of the business logo. It carries the business id (the browser caches the image, so each
     * business needs its own URL) and a version bumped on upload so a new logo is not served stale.
     */
    public String getBusinessLogoUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String ctx = fc != null ? fc.getExternalContext().getRequestContextPath() : "";
        return ctx + "/businessLogo?b=" + user.getBusinessId() + "&v=" + BusinessLogo.version(currentSession(false));
    }

    /** Initials of the business, shown as a monogram while it has no logo. */
    public String getBusinessInitials() {
        return initialsOf(getBusinessName());
    }

    public long getProductPhotoVersion() {
        return productPhotoVersion;
    }

    public void bumpProductPhotoVersion() {
        productPhotoVersion = System.currentTimeMillis();
    }

    /** Called after the logo is uploaded so the topbar picks up the new one. */
    public void refreshBusinessLogo() {
        BusinessLogo.invalidate(currentSession(false));
    }

    public boolean isBusinessVerified() {
        return user != null && Boolean.TRUE.equals(user.getBusinessVerified());
    }

    /** Uppercase initials (max 2) of the logged user's full name, for the avatar. */
    public String getInitials() {
        return initialsOf(user != null ? user.getFullName() : null);
    }

    /** Uppercase initials (max 2) of any name, null-safe ("?" when blank). Used by .avatar. */
    public String initialsOf(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1) {
            sb.append(Character.toUpperCase(parts[1].charAt(0)));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ formatting helpers

    /** "yyyy-MM-dd" -> "dd/MM/yyyy"; returns the input untouched when it does not parse. */
    public String fmtDay(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            return "";
        }
        try {
            return LocalDate.parse(yyyyMMdd.trim(), ISO_DAY).format(UI_DAY);
        } catch (Exception e) {
            return yyyyMMdd;
        }
    }

    /** Money with two decimals and a $ sign: "$1,234.50". Null -> "$0.00". */
    public String fmtMoney(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("$#,##0.00", symbols);
        return df.format(amount != null ? amount : BigDecimal.ZERO);
    }

    /** Today's date as "yyyy-MM-dd" in the business time zone. */
    public String today() {
        return LocalDate.now(ZoneId.of(Constants.TIMEZONE)).format(ISO_DAY);
    }

    /** Today's date already formatted for the UI ("dd/MM/yyyy"). */
    public String getTodayLabel() {
        return fmtDay(today());
    }

    /** Resolve an i18n key in the session locale (page beans use it for growl texts). */
    public String text(String key) {
        return label(ResourceBundle.getBundle("i18n.messages", getLocale()), key);
    }

    // ------------------------------------------------------------------ menu

    public MenuModel getMenuModel() {
        return menuModel;
    }

    private void buildMenu() {
        DefaultMenuModel model = new DefaultMenuModel();
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", getLocale());
        String contextPath = "";
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            contextPath = fc.getExternalContext().getRequestContextPath();
        }

        // group items by menuGroup, preserving order; skip app-only entries (no web outcome)
        Map<String, DefaultSubMenu> groups = new LinkedHashMap<>();
        List<MenuItemVo> items = user.getMenu();
        if (items != null) {
            for (MenuItemVo item : items) {
                if (item.getOutcomeWeb() == null || item.getOutcomeWeb().isBlank()) {
                    continue;
                }
                String groupKey = item.getMenuGroup() != null ? item.getMenuGroup() : "GENERAL";
                DefaultSubMenu sub = groups.get(groupKey);
                if (sub == null) {
                    sub = DefaultSubMenu.builder()
                            .label(label(bundle, "menu.group." + groupKey.toLowerCase()))
                            .build();
                    groups.put(groupKey, sub);
                }
                DefaultMenuItem menuItem = DefaultMenuItem.builder()
                        .value(label(bundle, item.getName()))
                        .icon(item.getIcon())
                        .url(contextPath + item.getOutcomeWeb())
                        .build();
                sub.getElements().add(menuItem);
            }
        }
        for (DefaultSubMenu sub : groups.values()) {
            model.getElements().add(sub);
        }
        this.menuModel = model;
    }

    private String label(ResourceBundle bundle, String key) {
        if (key == null) {
            return "";
        }
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public String logout() {
        HttpSession session = currentSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "/login.xhtml?faces-redirect=true";
    }

    private HttpSession currentSession(boolean create) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
        return request.getSession(create);
    }
}
