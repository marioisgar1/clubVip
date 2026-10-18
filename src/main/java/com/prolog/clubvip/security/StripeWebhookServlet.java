package com.prolog.clubvip.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.prolog.clubvip.constant.Constants;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Stripe webhook endpoint ({@code https://clubvip.club/stripeWebhook}). Stripe can only reach this
 * portal (the backend listens on localhost), so the request is passed on untouched: the raw body and
 * the {@code Stripe-Signature} header go to membership/webhook, which checks the signature. The
 * backend's status is answered back, so Stripe retries when the event could not be applied.
 */
@WebServlet("/stripeWebhook")
public class StripeWebhookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // The signature covers the exact bytes Stripe sent: never decode or re-encode them.
        byte[] body = request.getInputStream().readAllBytes();
        HttpURLConnection conn = (HttpURLConnection) new URL(Constants.BASE_URL + "membership/webhook").openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            String signature = request.getHeader("Stripe-Signature");
            if (signature != null) {
                conn.setRequestProperty("Stripe-Signature", signature);
            }
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }
            int status = conn.getResponseCode();
            response.setStatus(status);
            InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
            if (is != null) {
                try (is) {
                    is.readAllBytes();
                }
            }
        } catch (IOException e) {
            log("Stripe webhook could not be forwarded to the backend: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY); // Stripe retries later
        } finally {
            conn.disconnect();
        }
    }
}
