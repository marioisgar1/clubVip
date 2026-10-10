package com.prolog.clubvip.view;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Templates of "Mi página" and the color math behind the free accent color. The owner can pick
 * ANY color; the tones used for text, buttons and tints are derived here so the page stays
 * readable (WCAG contrast). resources/js/mypage.js repeats the same math for the live preview.
 */
public final class PageTheme implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String background;
    private final boolean dark;
    private final String fonts;
    private final String defaultColor;

    private PageTheme(String code, String background, boolean dark, String fonts, String defaultColor) {
        this.code = code;
        this.background = background;
        this.dark = dark;
        this.fonts = fonts;
        this.defaultColor = defaultColor;
    }

    private static final String GF = "https://fonts.googleapis.com/css2?";

    /** Same codes and order as BusinessPage.TEMPLATES in the backend. */
    public static final List<PageTheme> ALL = Collections.unmodifiableList(Arrays.asList(
            new PageTheme("ELEGANTE", "#0E0E10", true,
                    "family=Playfair+Display:ital,wght@0,500;0,600;1,500&family=Inter:wght@400;500;600;700", "#D4AF37"),
            new PageTheme("LUMINOSA", "#FFFFFF", false,
                    "family=Plus+Jakarta+Sans:wght@400;500;600;700;800", "#0F7C77"),
            new PageTheme("MERCADO", "#FFF4E6", false,
                    "family=Bricolage+Grotesque:opsz,wght@12..96,500;12..96,700;12..96,800&family=Nunito:wght@400;600;700;800", "#E4572E"),
            new PageTheme("MINIMAL", "#FFFFFF", false,
                    "family=Inter+Tight:wght@400;500;600;700&family=Inter:wght@400;500;600", "#1F2937"),
            new PageTheme("EDITORIAL", "#F7F5F0", false,
                    "family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@400;500;700", "#9A3B2E"),
            new PageTheme("NOCHE", "#0F1220", true,
                    "family=Outfit:wght@300;400;500;600;700", "#7C9CFF"),
            new PageTheme("NATURAL", "#EEF1EC", false,
                    "family=Fraunces:opsz,wght@9..144,400;9..144,600&family=Karla:wght@400;500;700", "#4F7A4A"),
            new PageTheme("URBANO", "#FFFFFF", false,
                    "family=Archivo+Black&family=Archivo:wght@400;500;700", "#FF4D2E"),
            new PageTheme("BOUTIQUE", "#FAF7F5", false,
                    "family=Cormorant+Garamond:ital,wght@0,500;0,600;1,500&family=Montserrat:wght@400;500;600", "#B5838D"),
            new PageTheme("FRESCO", "#FFFFFF", false,
                    "family=Poppins:wght@400;500;600;700", "#6C4CF1")));

    public static PageTheme of(String code) {
        for (PageTheme t : ALL) {
            if (t.code.equalsIgnoreCase(code != null ? code : "")) {
                return t;
            }
        }
        return ALL.get(0);
    }

    public String getCode() {
        return code;
    }

    /** CSS class of the page body: pg-elegante, pg-luminosa... */
    public String getCssClass() {
        return "pg-" + code.toLowerCase(Locale.ROOT);
    }

    public String getBackground() {
        return background;
    }

    public boolean isDark() {
        return dark;
    }

    public String getFontsUrl() {
        return GF + fonts + "&display=swap";
    }

    public String getDefaultColor() {
        return defaultColor;
    }

    // ------------------------------------------------------------------ color math

    /**
     * CSS custom properties for an accent color on this template:
     * --accent (fills, at least 1.8:1 against the page), --on-accent (text on fills),
     * --accent-text (4.5:1 for text on the page), --accent-soft / --accent-line (tints).
     */
    public String colorVars(String hex) {
        int[] bg = rgb(background);
        int[] base = rgb(validHex(hex) ? hex : defaultColor);
        int[] fill = ensure(base, bg, 1.8);
        int[] text = ensure(base, bg, 4.5);
        int[] white = { 255, 255, 255 };
        int[] ink = { 20, 22, 26 };
        int[] on = contrast(white, fill) >= contrast(ink, fill) ? white : ink;
        return "--accent:" + hex(fill) + ";--on-accent:" + hex(on) + ";--accent-text:" + hex(text)
                + ";--accent-soft:" + hex(mix(base, bg, dark ? 0.20 : 0.12))
                + ";--accent-line:" + hex(mix(base, bg, dark ? 0.45 : 0.35));
    }

    public static boolean validHex(String hex) {
        return hex != null && hex.matches("^#[0-9A-Fa-f]{6}$");
    }

    /** Moves the color toward black (light page) or white (dark page) until it reaches the ratio. */
    private int[] ensure(int[] color, int[] bg, double ratio) {
        int[] target = luminance(bg) > 0.5 ? new int[] { 0, 0, 0 } : new int[] { 255, 255, 255 };
        int[] c = color;
        for (int i = 0; i < 20 && contrast(c, bg) < ratio; i++) {
            c = mix(target, color, (i + 1) * 0.05);
        }
        return c;
    }

    /** {@code amount} of {@code a} over {@code b}. */
    private static int[] mix(int[] a, int[] b, double amount) {
        int[] r = new int[3];
        for (int i = 0; i < 3; i++) {
            r[i] = (int) Math.round(a[i] * amount + b[i] * (1 - amount));
        }
        return r;
    }

    private static double contrast(int[] a, int[] b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(int[] c) {
        double[] ch = new double[3];
        for (int i = 0; i < 3; i++) {
            double v = c[i] / 255.0;
            ch[i] = v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * ch[0] + 0.7152 * ch[1] + 0.0722 * ch[2];
    }

    private static int[] rgb(String hex) {
        String h = hex.substring(1);
        return new int[] { Integer.parseInt(h.substring(0, 2), 16), Integer.parseInt(h.substring(2, 4), 16),
                Integer.parseInt(h.substring(4, 6), 16) };
    }

    private static String hex(int[] c) {
        return String.format("#%02X%02X%02X", c[0], c[1], c[2]);
    }
}
