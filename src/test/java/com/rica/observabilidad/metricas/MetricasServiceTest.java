package com.rica.observabilidad.metricas;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricasServiceTest {
    private MetricasService metricas;

    @BeforeEach
    public void setup() {
        MeterRegistry registry = new SimpleMeterRegistry();
        metricas = new MetricasService(registry);
    }

    @Test
    public void testRegistrarExito() {
        metricas.registrarExito("operacion1");
        metricas.registrarExito("operacion1");
        metricas.registrarExito("operacion2");
        
        assertEquals(3.0, 
            metricas.meterRegistry.find("operaciones.exitosas").counter().count(), 0.01);
    }

    @Test
    public void testRegistrarFallo() {
        metricas.registrarFallo("operacion1");
        metricas.registrarFallo("operacion1");
        
        assertEquals(2.0,
            metricas.meterRegistry.find("operaciones.fallidas").counter().count(), 0.01);
    }

    @Test
    public void testMedirTiempoExitoso() {
        String resultado = metricas.medirTiempo("operacion1", () -> "Resultado");
        
        assertEquals("Resultado", resultado);
        assertTrue(metricas.meterRegistry.find("operaciones.tiempo").timer() != null);
    }

    @Test
    public void testMedirTiempoConFallo() {
        assertThrows(RuntimeException.class, () -> {
            metricas.medirTiempo("operacion1", () -> {
                throw new RuntimeException("Error");
            });
        });
    }

    public MeterRegistry getMeterRegistry() {
        return metricas.meterRegistry;
    }
}
