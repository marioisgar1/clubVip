package com.prolog.clubvip.view;

import java.util.Locale;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.MessageComposeVo;
import com.prolog.clubvip.vo.MessageLogRequestVo;

/**
 * Compose a WhatsApp message from a business template, hand the wa.me link back to the caller
 * (who opens it in a new tab) and record it in the message log.
 */
public final class WhatsappSender {

    private WhatsappSender() {
    }

    /** GET message/compose for the template/customer/appointment. Throws RestException. */
    public static MessageComposeVo compose(String templateType, Integer customerId, Integer appointmentId,
                                           String promo, Integer userId, Locale locale) {
        StringBuilder path = new StringBuilder("message/compose?templateType=").append(templateType);
        if (customerId != null) {
            path.append("&customerId=").append(customerId);
        }
        if (appointmentId != null) {
            path.append("&appointmentId=").append(appointmentId);
        }
        if (promo != null && !promo.isBlank()) {
            path.append("&promo=").append(RestClient.encode(promo.trim()));
        }
        return RestClient.get(path.toString(), MessageComposeVo.class, userId, locale);
    }

    /** POST message/logSent with the composed message; failures are swallowed (the message was already opened). */
    public static void log(MessageComposeVo compose, Integer appointmentId, Integer userId, Locale locale) {
        if (compose == null) {
            return;
        }
        try {
            MessageLogRequestVo log = new MessageLogRequestVo();
            log.setCustomerId(compose.getCustomerId());
            log.setAppointmentId(appointmentId);
            log.setTemplateType(compose.getTemplateType());
            log.setPhone(compose.getPhone());
            log.setBody(compose.getBody());
            RestClient.post("message/logSent", log, userId, locale);
        } catch (RestException ignored) {
            // logging must never block the operator
        }
    }
}
