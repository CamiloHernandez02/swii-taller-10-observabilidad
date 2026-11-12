package com.rica.observability;

import java.util.UUID;

public class CorrelationIdHolder {
    private static final ThreadLocal<String> correlationIdThreadLocal = new ThreadLocal<>();

    public static String getCorrelationId() {
        String correlationId = correlationIdThreadLocal.get();
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            correlationIdThreadLocal.set(correlationId);
        }
        return correlationId;
    }

    public static void setCorrelationId(String correlationId) {
        correlationIdThreadLocal.set(correlationId);
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static void clear() {
        correlationIdThreadLocal.remove();
    }
}
