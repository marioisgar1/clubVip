package com.prolog.clubvip.view;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Icons offered by the icon pickers ({@code #{iconCatalog.icons}}): every PrimeIcons class of the
 * bundled PrimeFaces, read from its primeicons.css, so the list always matches the icons that render.
 * The ones that fit a business come first; the rest follow alphabetically.
 */
@Named("iconCatalog")
@ApplicationScoped
public class IconCatalog {

    private static final String CSS = "META-INF/resources/primefaces/primeicons/primeicons.css";
    private static final Pattern ICON = Pattern.compile("\\.pi-([a-z0-9-]+):before");

    /** Shown first (only those that exist in the bundled PrimeIcons). */
    private static final List<String> SUGGESTED = Arrays.asList(
            "heart", "star", "shopping-bag", "shopping-cart", "wrench", "car", "truck", "home", "building",
            "user", "users", "calendar", "clock", "palette", "camera", "image", "gift", "tag", "tags",
            "briefcase", "book", "graduation-cap", "hammer", "bolt", "sun", "moon", "face-smile", "crown",
            "sparkles", "globe", "map-marker", "phone", "mobile", "desktop", "headphones", "microphone",
            "video", "money-bill", "wallet", "credit-card", "shield", "verified", "github", "trophy");

    private volatile List<String> icons;

    /** Full classes ("pi pi-heart"), suggested first. */
    public List<String> getIcons() {
        List<String> list = icons;
        if (list == null) {
            list = load();
            icons = list;
        }
        return list;
    }

    private static List<String> load() {
        Set<String> available = new TreeSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl != null ? cl.getResourceAsStream(CSS) : null) {
            if (in != null) {
                Matcher m = ICON.matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                while (m.find()) {
                    available.add(m.group(1));
                }
            }
        } catch (Exception e) {
            available.clear();
        }
        Set<String> ordered = new LinkedHashSet<>();
        for (String name : SUGGESTED) {
            if (available.isEmpty() || available.contains(name)) {
                ordered.add(name);
            }
        }
        ordered.addAll(available);
        List<String> result = new ArrayList<>();
        for (String name : ordered) {
            result.add("pi pi-" + name);
        }
        return Collections.unmodifiableList(result);
    }
}
