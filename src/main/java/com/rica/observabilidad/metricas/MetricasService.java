package com.rica.observabilidad.metricas;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricasService {
    private final MeterRegistry meterRegistry;
    private final Counter operacionesExitosas;
    private final Counter operacionesFallidas;
    private final Timer tiempoOperaciones;

    public MetricasService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.operacionesExitosas = Counter.builder("operaciones.exitosas")
            .description("Total de operaciones exitosas")
            .register(meterRegistry);
        this.operacionesFallidas = Counter.builder("operaciones.fallidas")
            .description("Total de operaciones fallidas")
            .register(meterRegistry);
        this.tiempoOperaciones = Timer.builder("operaciones.tiempo")
            .description("Tiempo de ejecución de operaciones")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
    }

    public void registrarExito(String operacion) {
        operacionesExitosas.increment();
        meterRegistry.counter("operaciones.exitosas.por.tipo", "operacion", operacion).increment();
    }

    public void registrarFallo(String operacion) {
        operacionesFallidas.increment();
        meterRegistry.counter("operaciones.fallidas.por.tipo", "operacion", operacion).increment();
    }

    public <T> T medirTiempo(String operacion, java.util.function.Supplier<T> accion) {
        return tiempoOperaciones.recordCallable(() -> {
            try {
                T resultado = accion.get();
                registrarExito(operacion);
                return resultado;
            } catch (Exception e) {
                registrarFallo(operacion);
                throw e;
            }
        });
    }
}
