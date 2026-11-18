package com.rica.investigadores.observabilidad;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricasServiceTest {

    private MetricasService metricas;
    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metricas = new MetricasService(registry);
    }

    @Test
    void testIncrementarRequestCount() {
        metricas.incrementarRequestCount();
        metricas.incrementarRequestCount();

        double count = registry.counter("http.requests.total").count();
        assertEquals(2.0, count);
    }

    @Test
    void testIncrementarErrorCount() {
        metricas.incrementarErrorCount();
        metricas.incrementarErrorCount();

        double count = registry.counter("http.errors.total").count();
        assertEquals(2.0, count);
    }

    @Test
    void testTimer() {
        Timer.Sample sample = metricas.iniciarTimer();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metricas.detenerTimer(sample);

        Timer timer = registry.find("http.requests.duration").timer();
        assertNotNull(timer);
        assertTrue(timer.count() > 0);
    }

    @Test
    void testTimerNull() {
        metricas.detenerTimer(null);
        Timer timer = registry.find("http.requests.duration").timer();
        assertNotNull(timer);
        assertEquals(0, timer.count());
    }
}
