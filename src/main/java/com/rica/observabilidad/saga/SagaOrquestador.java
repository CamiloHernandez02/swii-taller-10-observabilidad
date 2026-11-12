package com.rica.observabilidad.saga;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Saga Pattern: Implementa transacciones distribuidas con compensaciones.
 * Coordina múltiples servicios independientes con rollback en caso de fallo.
 */
@Service
public class SagaOrquestador {
    private static final Logger logger = LoggerFactory.getLogger(SagaOrquestador.class);
    private final List<Paso> pasos = new ArrayList<>();
    private final List<Paso> compensaciones = new ArrayList<>();

    public void agregarPaso(String nombre, Runnable accion, Runnable compensacion) {
        pasos.add(new Paso(nombre, accion));
        compensaciones.add(0, new Paso(nombre, compensacion)); // Agregar al inicio para LIFO
    }

    public void ejecutarSaga() throws Exception {
        int pasosEjecutados = 0;
        try {
            for (Paso paso : pasos) {
                logger.info("▶️ Ejecutando paso: {}", paso.nombre);
                paso.accion.run();
                logger.info("✅ Paso completado: {}", paso.nombre);
                pasosEjecutados++;
            }
            logger.info("🎉 Saga completada exitosamente");
        } catch (Exception e) {
            logger.error("❌ Error en saga, ejecutando compensaciones");
            ejecutarCompensaciones(pasosEjecutados);
            throw new SagaFallidaException("Saga falló en paso " + pasosEjecutados, e);
        }
    }

    private void ejecutarCompensaciones(int pasosEjecutados) {
        for (int i = 0; i < pasosEjecutados && i < compensaciones.size(); i++) {
            Paso compensacion = compensaciones.get(i);
            try {
                logger.info("↩️ Compensando: {}", compensacion.nombre);
                compensacion.accion.run();
                logger.info("✅ Compensación completada: {}", compensacion.nombre);
            } catch (Exception e) {
                logger.error("⚠️ Error en compensación de {}", compensacion.nombre, e);
            }
        }
    }

    public static class SagaFallidaException extends Exception {
        public SagaFallidaException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private static class Paso {
        String nombre;
        Runnable accion;

        Paso(String nombre, Runnable accion) {
            this.nombre = nombre;
            this.accion = accion;
        }
    }
}
