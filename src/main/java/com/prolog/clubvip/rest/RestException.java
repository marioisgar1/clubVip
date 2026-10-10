package com.prolog.clubvip.rest;

import java.util.List;

/**
 * Runtime error carrying the backend's i18n-resolved messages.
 */
public class RestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> messages;

    public RestException(String message) {
        super(message);
        this.messages = List.of(message != null ? message : "error");
    }

    public RestException(List<String> messages) {
        super(messages != null && !messages.isEmpty() ? messages.get(0) : "error");
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
