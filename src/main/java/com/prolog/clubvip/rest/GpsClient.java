package com.prolog.clubvip.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.prolog.clubvip.constant.Constants;

/**
 * Read-only client of the clubVipGps service (devices connected, frames, statistics). Same Basic
 * user as the backend gateway. Its dates come as ISO-8601 text (Instant with nanoseconds), so the
 * VOs keep them as String and {@link com.prolog.clubvip.view.GpsFmt} formats them.
 */
public final class GpsClient {

    private static final Gson GSON = new Gson();

    private GpsClient() {
    }

    /** GET returning the parsed body; RestException when the service is down or answers an error. */
    public static <T> T get(String path, Type type) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(Constants.GPS_BASE_URL + path).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            String raw = Constants.GATEWAY_USER + ":" + Constants.GATEWAY_PASSWORD;
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
            conn.setRequestProperty("Accept", "application/json");
            int status = conn.getResponseCode();
            InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            }
            if (status >= 300) {
                throw new RestException("GPS " + status + ": " + sb);
            }
            return sb.length() == 0 ? null : GSON.fromJson(sb.toString(), type);
        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            throw new RestException(e.getMessage());
        }
    }
}
