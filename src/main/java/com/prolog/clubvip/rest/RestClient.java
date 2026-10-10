package com.prolog.clubvip.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolog.clubvip.constant.Constants;
import com.prolog.clubvip.vo.GenericErrorVo;

/**
 * Minimal REST client (HttpURLConnection + Gson) that talks to the backend gateway.
 * Every call sends the gateway Basic auth, the acting user (X-Audit-User), the locale
 * (Accept-Language) and the business time zone (X-Timezone).
 */
public final class RestClient {

    /**
     * Dates travel as ISO-8601 both ways: the default Gson output ("Oct 2, 2026, 8:17:14 PM") is
     * rejected by the backend, e.g. when a screen posts back the createdAt it received.
     */
    private static final Gson GSON = new com.google.gson.GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .create();

    private RestClient() {
    }

    private static String basicAuth() {
        String raw = Constants.GATEWAY_USER + ":" + Constants.GATEWAY_PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static HttpURLConnection open(String path, String method, Integer userId, Locale locale)
            throws Exception {
        URL url = new URL(Constants.BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Authorization", basicAuth());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Language", locale != null ? locale.getLanguage() : "es");
        // Reports and "today" are cut in the business zone, never in the server's UTC.
        conn.setRequestProperty("X-Timezone", Constants.TIMEZONE);
        if (userId != null) {
            conn.setRequestProperty("X-Audit-User", String.valueOf(userId));
        }
        return conn;
    }

    private static String readBody(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** GET returning a parsed object of the given type (Class or TypeToken#getType). Throws RestException. */
    public static <T> T get(String path, Type type, Integer userId, Locale locale) {
        try {
            HttpURLConnection conn = open(path, "GET", userId, locale);
            String body = readBody(conn);
            if (conn.getResponseCode() >= 300) {
                throw new RestException(extractErrors(body));
            }
            if (body == null || body.isBlank()) {
                return null; // 204 No Content
            }
            return GSON.fromJson(body, type);
        } catch (RestException re) {
            throw re;
        } catch (Exception e) {
            throw new RestException(e.getMessage());
        }
    }

    /** POST a JSON body; returns raw status + body so the caller can react. */
    public static RestResponse post(String path, Object payload, Integer userId, Locale locale) {
        return send(path, "POST", payload, userId, locale);
    }

    /**
     * POST a JSON body and parse the response into the given type, or throw RestException
     * with the backend's translated messages when the status is not 2xx.
     */
    public static <T> T post(String path, Object payload, Type type, Integer userId, Locale locale) {
        RestResponse response = send(path, "POST", payload, userId, locale);
        if (!response.isOk()) {
            throw new RestException(extractErrors(response.getBody()));
        }
        String body = response.getBody();
        if (type == null || body == null || body.isBlank()) {
            return null;
        }
        try {
            return GSON.fromJson(body, type);
        } catch (Exception e) {
            throw new RestException(e.getMessage());
        }
    }

    public static RestResponse delete(String path, Integer userId, Locale locale) {
        return send(path, "DELETE", null, userId, locale);
    }

    private static RestResponse send(String path, String method, Object payload, Integer userId, Locale locale) {
        try {
            HttpURLConnection conn = open(path, method, userId, locale);
            if (payload != null) {
                conn.setDoOutput(true);
                byte[] out = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(out);
                }
            }
            String body = readBody(conn);
            return new RestResponse(conn.getResponseCode(), body);
        } catch (Exception e) {
            return new RestResponse(500, "[{\"message\":\"" + safe(e.getMessage()) + "\"}]");
        }
    }

    /** Parse the standard List&lt;GenericException&gt; error body into human messages. */
    public static List<String> extractErrors(String body) {
        List<String> messages = new ArrayList<>();
        try {
            Type t = new TypeToken<List<GenericErrorVo>>() { }.getType();
            List<GenericErrorVo> errors = GSON.fromJson(body, t);
            if (errors != null) {
                for (GenericErrorVo e : errors) {
                    if (e != null && e.getMessage() != null) {
                        messages.add(e.getMessage());
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        if (messages.isEmpty() && body != null && !body.isBlank()) {
            messages.add(body);
        }
        return messages;
    }

    /** URL-encode a query string value (UTF-8). */
    public static String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private static String safe(String s) {
        return s == null ? "error" : s.replace("\"", "'");
    }
}
