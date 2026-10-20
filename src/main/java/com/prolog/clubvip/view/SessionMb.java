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
import com.prolog.clubvip.security.UserPhoto;
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
    private String myPhotoData;
    private String logoTone;
    private int logoToneVersion = -1;
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

    /** The business turned Rewards Club VIP (points) on: Programa de lealtad shows in the menu. */
    public boolean isPointsEnabled() {
        return user == null || !Boolean.FALSE.equals(user.getPointsEnabled());
    }

    /**
     * Rewards is on AND configured (an active loyalty program in Programa de lealtad): only then checkout asks
     * for the member card. An older backend without the field falls back to {@link #isPointsEnabled()}.
     */
    public boolean isRewardsReady() {
        if (user != null && user.getRewardsReady() != null) {
            return user.getRewardsReady();
        }
        return isPointsEnabled();
    }

    /** After saving the loyalty program the checkout asks for the card at once (no new login needed). */
    public void markRewardsConfigured() {
        if (user != null) {
            user.setRewardsReady(isPointsEnabled());
        }
    }

    /** True unless the business explicitly switched the sales module off. */
    public boolean isSalesEnabled() {
        return user == null || !Boolean.FALSE.equals(user.getModuleSales());
    }

    /** The home page is the point of sale: sellers, and admins whose business has no appointments. */
    /** Header of the payroll count column: services, sales, or both, by the modules the business uses. */
    public String getPayrollCountLabel() {
        if (!isAppointmentsEnabled()) {
            return text("rel.payroll.sales");
        }
        if (!isSalesEnabled()) {
            return text("rel.payroll.services");
        }
        return text("rel.payroll.servicesSales");
    }

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
        // trim=1: without the transparent 4:1 padding, so a square or round logo is not lost in a strip.
        return ctx + "/businessLogo?b=" + user.getBusinessId() + "&trim=1&v=" + BusinessLogo.version(currentSession(false));
    }

    /** Browser-tab icon: the logo trimmed and centred on a transparent square. */
    public String getBusinessIconUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String ctx = fc != null ? fc.getExternalContext().getRequestContextPath() : "";
        return ctx + "/businessLogo?b=" + user.getBusinessId() + "&icon=1&v=" + BusinessLogo.version(currentSession(false));
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

    /** The logged user has a photo: the menu avatar shows it instead of the initials. */
    public boolean isHasUserPhoto() {
        return user != null && UserPhoto.of(currentSession(false)).length > 0;
    }

    /** URL of the user's photo, with the user id and a version bumped when the photo changes. */
    public String getUserPhotoUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String ctx = fc != null ? fc.getExternalContext().getRequestContextPath() : "";
        return ctx + "/userPhoto?u=" + user.getUserId() + "&v=" + UserPhoto.version(currentSession(false));
    }

    /** JPEG data URL framed by photo-crop.js from the menu avatar, sent by {@link #uploadMyPhoto()}. */
    public String getMyPhotoData() {
        return myPhotoData;
    }

    public void setMyPhotoData(String myPhotoData) {
        this.myPhotoData = myPhotoData;
    }

    /** Saves the photo picked from the menu avatar (user/uploadMyPhoto) and reloads the avatar. */
    public void uploadMyPhoto() {
        String data = myPhotoData;
        myPhotoData = null;
        if (user == null || data == null || data.isBlank()) {
            return;
        }
        int comma = data.indexOf(',');
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("imageBase64", comma >= 0 ? data.substring(comma + 1) : data);
        com.prolog.clubvip.rest.RestResponse r = com.prolog.clubvip.rest.RestClient.post("user/uploadMyPhoto", payload,
                user.getUserId(), getLocale());
        FacesContext fc = FacesContext.getCurrentInstance();
        if (r.isOk()) {
            UserPhoto.invalidate(currentSession(false));
            fc.addMessage(null, new jakarta.faces.application.FacesMessage(text("menu.photo.saved")));
        } else {
            for (String e : com.prolog.clubvip.rest.RestClient.extractErrors(r.getBody())) {
                fc.addMessage(null, new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_ERROR, e, null));
            }
        }
    }

    /** "Administrador", "Empleado"... of the logged profile (the API only sends its code). */
    public String getProfileLabel() {
        if (user == null || user.getProfileId() == null) {
            return "";
        }
        String label = text("menu.profile." + user.getProfileId());
        return label.startsWith("???") || label.startsWith("menu.profile.") ? String.valueOf(user.getProfileName()) : label;
    }

    /** "light" or "dark" artwork of the business logo: the menu shows it on an ink or white card. */
    public String getBusinessLogoTone() {
        HttpSession session = currentSession(false);
        int version = BusinessLogo.version(session);
        if (logoTone == null || logoToneVersion != version) {
            logoTone = BusinessLogo.tone(BusinessLogo.of(session));
            logoToneVersion = version;
        }
        return logoTone;
    }

    /** Called after an employee is saved: when it is the logged user, the avatar reloads the photo. */
    public void refreshUserPhoto(Integer employeeId) {
        if (user != null && employeeId != null && employeeId.equals(user.getEmployeeId())) {
            UserPhoto.invalidate(currentSession(false));
        }
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

    /** "yyyy-MM-dd" + "HH:mm" -> "dd/MM/yyyy HH:mm"; only the day when the time is missing. */
    public String fmtDayTime(String yyyyMMdd, String hhmm) {
        String day = fmtDay(yyyyMMdd);
        if (hhmm == null || hhmm.isBlank()) {
            return day;
        }
        String t = hhmm.trim();
        return day.isEmpty() ? t : day + " " + (t.length() > 5 ? t.substring(0, 5) : t);
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

    /** {@link #text(String)} with {0}, {1}... placeholders filled in. */
    public String textf(String key, Object... args) {
        return java.text.MessageFormat.format(text(key), args);
    }

    // ------------------------------------------------------------------ menu

    public MenuModel getMenuModel() {
        return menuModel;
    }

    /** Order of the menu groups; the group and order of each entry come from the permission table
     *  (the app drawer reads the same data, see clubVipApp Modules.GROUP_ORDER). The groups follow
     *  the modules of sign-up: each module switched on adds its group (database/menu_by_module.sql). */
    private static final List<String> WEB_GROUP_ORDER = List.of(
            "HOME", "APPOINTMENTS", "SALES", "CUSTOMERS", "INVENTORY", "SALES_REPORTS", "TEAM",
            "BUSINESS", "BACKOFFICE", "GPS", "SECURITY", "CLIENT",
            // groups before menu_by_module.sql, kept so an older database still renders in order
            "SETUP", "OPERATION", "CATALOGS", "WAREHOUSE", "RELATION", "ADMIN", "REPORTS");

    /** Groups shown without a title: the start of the menu. */
    private static final java.util.Set<String> UNTITLED_GROUPS = java.util.Set.of("HOME");

    /** Configuration groups: drawn quieter, at the end. */
    private static final java.util.Set<String> SETUP_GROUPS = java.util.Set.of("BUSINESS", "SETUP");

    /** Screens still under construction: listed with a "Próximamente" badge but not clickable
     *  (same list as the app, Modules.COMING_SOON). */
    private static final java.util.Set<String> COMING_SOON = java.util.Set.of("LOYALTY_VIEW", "POINTS_VIEW");

    /** "A la mano" shortcuts in order of preference: permission code, i18n label, icon. */
    private static final String[][] QUICK_LINKS = {
            {"APPOINTMENT_VIEW", "quick.todayAppointments", "pi pi-clock"},
            {"MY_APPOINTMENTS", "quick.myAppointments", "pi pi-calendar"},
            {"SCHEDULE_VIEW", "quick.schedule", "pi pi-calendar-plus"},
            {"SALE_NEW", "quick.sell", "pi pi-shopping-cart"},
            {"CUSTOMER_VIEW", "quick.customers", "pi pi-users"},
            {"MY_PAYROLL", "quick.myPayroll", "pi pi-wallet"},
            {"INVENTORY_VIEW", "quick.stock", "pi pi-box"}
    };

    /** One "A la mano" tile. */
    public static class QuickLink implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String label;
        private final String icon;
        private final String url;

        QuickLink(String label, String icon, String url) {
            this.label = label;
            this.icon = icon;
            this.url = url;
        }

        public String getLabel() { return label; }
        public String getIcon() { return icon; }
        public String getUrl() { return url; }
    }

    private List<QuickLink> quickLinks = new java.util.ArrayList<>();

    /** Up to four shortcuts to the most used screens this user can open (built with the menu). */
    public List<QuickLink> getQuickLinks() {
        return quickLinks;
    }

    /** Number of web menu entries (the sidebar search only shows when the menu is long). */
    private int menuSize;

    public int getMenuSize() {
        return menuSize;
    }

    private static String webGroup(MenuItemVo item) {
        return item.getMenuGroup() != null ? item.getMenuGroup() : "GENERAL";
    }

    private static int webSortOrder(MenuItemVo item) {
        return item.getSortOrder() != null ? item.getSortOrder() : Integer.MAX_VALUE;
    }

    private static int webGroupRank(MenuItemVo item) {
        int i = WEB_GROUP_ORDER.indexOf(webGroup(item));
        return i < 0 ? WEB_GROUP_ORDER.size() : i;
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
        Map<String, List<MenuItemVo>> groups = new LinkedHashMap<>();
        List<MenuItemVo> items = user.getMenu() != null ? new java.util.ArrayList<>(user.getMenu()) : null;
        Map<String, MenuItemVo> byCode = new java.util.HashMap<>();
        int size = 0;
        if (items != null) {
            // Group order here, entry group and order from the permission table (same as the app).
            items.sort(java.util.Comparator.comparingInt(SessionMb::webGroupRank).thenComparingInt(SessionMb::webSortOrder));
            for (MenuItemVo item : items) {
                if (item.getOutcomeWeb() == null || item.getOutcomeWeb().isBlank()) {
                    continue;
                }
                groups.computeIfAbsent(webGroup(item), k -> new java.util.ArrayList<>()).add(item);
                if (item.getCode() != null) {
                    byCode.put(item.getCode(), item);
                }
                size++;
            }
        }
        for (Map.Entry<String, List<MenuItemVo>> g : groups.entrySet()) {
            String key = g.getKey();
            String css = "cv-g-" + key.toLowerCase() + (SETUP_GROUPS.contains(key) ? " cv-g-setup" : "");
            // Only the start of the menu (Inicio) goes without a title; every other entry lives in a named group.
            if (UNTITLED_GROUPS.contains(key)) {
                for (MenuItemVo item : g.getValue()) {
                    model.getElements().add(menuItem(bundle, contextPath, item, "cv-solo cv-home " + css));
                }
                continue;
            }
            DefaultSubMenu sub = DefaultSubMenu.builder()
                    .id("g_" + key.toLowerCase())
                    .label(label(bundle, "menu.group." + key.toLowerCase()))
                    .styleClass("cv-grp " + css)
                    .build();
            for (MenuItemVo item : g.getValue()) {
                sub.getElements().add(menuItem(bundle, contextPath, item, "cv-in " + css));
            }
            model.getElements().add(sub);
        }
        // "A la mano": the first four shortcuts of the list this user can open.
        List<QuickLink> quick = new java.util.ArrayList<>();
        for (String[] q : QUICK_LINKS) {
            MenuItemVo item = byCode.get(q[0]);
            if (item != null && !COMING_SOON.contains(item.getCode()) && quick.size() < 4) {
                quick.add(new QuickLink(label(bundle, q[1]), q[2], contextPath + item.getOutcomeWeb()));
            }
        }
        this.quickLinks = quick;
        this.menuSize = size;
        this.menuModel = model;
    }

    private DefaultMenuItem menuItem(ResourceBundle bundle, String contextPath, MenuItemVo item, String containerCss) {
        if (item.getCode() != null && COMING_SOON.contains(item.getCode())) {
            // No url and disabled: the entry shows but does not open; the badge is part of the label.
            String soon = label(bundle, "menu.comingSoon");
            return DefaultMenuItem.builder()
                    .value(html(label(bundle, item.getName()))
                            + "<span class=\"cv-soon-badge\"><i class=\"pi pi-bolt\"></i>" + html(soon) + "</span>")
                    .escape(false)
                    .icon(item.getIcon())
                    .disabled(true)
                    .title(soon)
                    .containerStyleClass(containerCss + " cv-soon")
                    .build();
        }
        return DefaultMenuItem.builder()
                .value(label(bundle, item.getName()))
                .icon(item.getIcon())
                .url(contextPath + item.getOutcomeWeb())
                .containerStyleClass(containerCss)
                .build();
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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

    /** Last day of the membership grace period as "15 de octubre de 2026" (portal banner). */
    public String getMembershipGraceText() {
        String iso = user != null ? user.getMembershipGraceUntil() : null;
        if (iso == null || iso.isBlank()) {
            return "";
        }
        try {
            String pattern = "en".equals(getLanguage()) ? "MMMM d, yyyy" : "d 'de' MMMM 'de' yyyy";
            return java.time.LocalDate.parse(iso).format(java.time.format.DateTimeFormatter.ofPattern(pattern, getLocale()));
        } catch (RuntimeException e) {
            return iso;
        }
    }

    // ------------------------------------------------ membership labels (operator tables)

    /** "Cortesía" / "Mensual · Stripe" / "Anual · Stripe", or "" when the business has no plan. */
    public String planLabel(com.prolog.clubvip.vo.BusinessVo b) {
        String plan = b != null ? b.getMembershipPlan() : null;
        if (plan == null || plan.isBlank()) {
            return "";
        }
        return text("plan." + plan.toUpperCase());
    }

    public String planIcon(com.prolog.clubvip.vo.BusinessVo b) {
        return "COURTESY".equals(b.getMembershipPlan()) ? "pi pi-gift" : "pi pi-credit-card";
    }

    /** "hasta el 31/12/2026", "venció el 01/10/2026", "Sin vencimiento" (courtesy only) or "". */
    public String untilLabel(com.prolog.clubvip.vo.BusinessVo b) {
        String iso = b.getSubscriptionUntil();
        if (iso == null || iso.isBlank()) {
            return "COURTESY".equals(b.getMembershipPlan()) ? text("mem.until.none") : "";
        }
        return text(isMembershipExpired(b) ? "mem.until.expired" : "mem.until.on") + " " + fmtDay(iso);
    }

    public boolean isMembershipExpired(com.prolog.clubvip.vo.BusinessVo b) {
        String iso = b.getSubscriptionUntil();
        if (iso == null || iso.isBlank()) {
            return false;
        }
        try {
            return java.time.LocalDate.parse(iso).isBefore(java.time.LocalDate.now());
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Absolute URL of the home page, for its link preview (og:url). */
    public String getSiteUrl() {
        return PublicLinks.absolute("/");
    }

    /** Absolute URL of the home page share image (1200x630, og:image) for WhatsApp / Facebook previews. */
    public String getSiteShareImage() {
        return PublicLinks.absolute("/resources/img/og-clubvip.jpg?v=20261018");
    }
}
