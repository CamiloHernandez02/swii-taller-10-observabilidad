package com.rica.observabilidad.trazabilidad;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.TraceContext;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TrazabilidadService {
    private static final Logger logger = LoggerFactory.getLogger(TrazabilidadService.class);
    private final Tracer tracer;

    public TrazabilidadService(Tracer tracer) {
        this.tracer = tracer;
    }

    public void rastrearOperacion(String operacion, Runnable accion) {
        tracer.runWithSpan("operacion:" + operacion, () -> {
            logger.info("🔍 Iniciando operación: {}", operacion);
            try {
                accion.run();
                logger.info("✅ Operación completada: {}", operacion);
            } catch (Exception e) {
                logger.error("❌ Error en operación: {}", operacion, e);
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T rastrearOperacionConResultado(String operacion, java.util.function.Supplier<T> accion) {
        return tracer.traceWithSpan("operacion:" + operacion, span -> {
            logger.info("🔍 Iniciando operación: {}", operacion);
            try {
                T resultado = accion.get();
                logger.info("✅ Operación completada: {}", operacion);
                return resultado;
            } catch (Exception e) {
                logger.error("❌ Error en operación: {}", operacion, e);
                throw new RuntimeException(e);
            }
        });
    }
}
