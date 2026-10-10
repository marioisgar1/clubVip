package com.prolog.clubvip.view;

import com.prolog.clubvip.rest.RestClient;

/**
 * WhatsApp click-to-chat links with a pre-filled message. They go straight to
 * {@code api.whatsapp.com/send}: the {@code wa.me} redirect turns emojis (👋, 📅…) into "�".
 */
public final class WhatsAppLink {

    private WhatsAppLink() {
    }

    /**
     * Link to chat with {@code phone} (digits, or a {@code https://wa.me/<digits>} link as the
     * backend returns it) with {@code text} typed in. Null when there is no phone.
     */
    public static String withText(String phone, String text) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceFirst("^.*wa\\.me/", "").replaceFirst("\\?.*$", "").replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("https://api.whatsapp.com/send?phone=").append(digits);
        if (text != null && !text.isBlank()) {
            sb.append("&text=").append(RestClient.encode(text).replace("+", "%20"));
        }
        return sb.toString();
    }
}
