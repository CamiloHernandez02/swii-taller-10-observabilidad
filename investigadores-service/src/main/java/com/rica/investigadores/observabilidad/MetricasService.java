package com.rica.investigadores.observabilidad;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class MetricasService {
    private static final Logger logger = LoggerFactory.getLogger(MetricasService.class);
    private final MeterRegistry meterRegistry;

    private Counter requestCount;
    private Counter errorCount;
    private Timer requestTimer;

    public MetricasService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        requestCount = Counter.builder("http.requests.total")
                .description("Total de requests")
                .register(meterRegistry);

        errorCount = Counter.builder("http.errors.total")
                .description("Total de errores")
                .register(meterRegistry);

        requestTimer = Timer.builder("http.requests.duration")
                .description("Duración de requests en segundos")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void incrementarRequestCount() {
        requestCount.increment();
    }

    public void incrementarErrorCount() {
        errorCount.increment();
    }

    public Timer.Sample iniciarTimer() {
        return Timer.start(meterRegistry);
    }

    public void detenerTimer(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(requestTimer);
        }
    }
}
