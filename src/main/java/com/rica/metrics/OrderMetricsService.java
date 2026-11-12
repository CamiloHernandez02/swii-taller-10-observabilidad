package com.rica.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

@Service
public class OrderMetricsService {
    private final MeterRegistry meterRegistry;

    public OrderMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordOrderCreated(String status) {
        meterRegistry.counter("order.created", Tags.of("status", status)).increment();
    }

    public void recordOrderProcessingTime(long milliseconds) {
        meterRegistry.timer("order.processing.time").record(
                java.time.Duration.ofMillis(milliseconds)
        );
    }

    public void recordOrderTotal(double amount) {
        meterRegistry.gauge("order.total.amount", amount);
    }

    public void recordPaymentFailure(String reason) {
        meterRegistry.counter("payment.failed", Tags.of("reason", reason)).increment();
    }

    public void recordShipmentDelay(long hours) {
        meterRegistry.timer("shipment.delay").record(
                java.time.Duration.ofHours(hours)
        );
    }
}
