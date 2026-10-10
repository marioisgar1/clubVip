package com.prolog.clubvip.constant;

/**
 * Central configuration for reaching the backend REST gateway.
 * Adjust BASE_URL / credentials per environment (or externalize to a properties file).
 */
public final class Constants {

    private Constants() {
    }

    /** Backend base URL (clubVipServices). */
    public static final String BASE_URL = "http://localhost:8884/api/v1/";

    /** Gateway HTTP Basic credentials (must match gateway.user / gateway.password in the backend). */
    public static final String GATEWAY_USER = "developer";
    public static final String GATEWAY_PASSWORD = "Club.Vip.Dev";

    /** Session attribute holding the logged-in user (LoginResponseVo). */
    public static final String SESSION_USER = "userSession";

    /** Club VIP support WhatsApp number (international format, no '+'), used by wa.me links. */
    public static final String CLUBVIP_WHATSAPP = "5559396813";

    /**
     * Public address of the web for the links businesses share (Instagram, Facebook):
     * {@code <PUBLIC_BASE_URL>/b/{slug}/citas}. The JVM property {@code clubvip.publicBaseUrl}
     * overrides it; when it is blank the links are built from the current request (local tests).
     */
    public static final String PUBLIC_BASE_URL = System.getProperty("clubvip.publicBaseUrl", "");

    /** Android package of the app: "Abrir en la app" and the Play Store fallback. */
    public static final String APP_PACKAGE = "com.prolog.clubvip.rewards"; // customers book from Rewards Club VIP

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=" + APP_PACKAGE;

    /** Business time zone used to cut "today" and to format dates. */
    public static final String TIMEZONE = "America/Mexico_City";
}
