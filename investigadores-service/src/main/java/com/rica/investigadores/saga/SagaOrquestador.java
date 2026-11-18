package com.rica.investigadores.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SagaOrquestador {
    private static final Logger logger = LoggerFactory.getLogger(SagaOrquestador.class);

    public static class SagaStep {
        private final String nombre;
        private final Runnable accion;
        private final Consumer<Exception> compensacion;

        public SagaStep(String nombre, Runnable accion, Consumer<Exception> compensacion) {
            this.nombre = nombre;
            this.accion = accion;
            this.compensacion = compensacion;
        }

        public String getNombre() {
            return nombre;
        }

        public Runnable getAccion() {
            return accion;
        }

        public Consumer<Exception> getCompensacion() {
            return compensacion;
        }
    }

    private final List<SagaStep> steps = new ArrayList<>();
    private final List<SagaStep> ejecutados = new ArrayList<>();

    public void agregarStep(SagaStep step) {
        steps.add(step);
    }

    public void ejecutar() throws Exception {
        for (SagaStep step : steps) {
            try {
                logger.info("Ejecutando saga step: {}", step.getNombre());
                step.getAccion().run();
                ejecutados.add(step);
            } catch (Exception e) {
                logger.error("Error en saga step: {}. Iniciando rollback...", step.getNombre(), e);
                rollback();
                throw new SagaException("Saga falló en step: " + step.getNombre(), e);
            }
        }
        logger.info("Saga completada exitosamente");
    }

    private void rollback() {
        logger.info("Iniciando compensaciones (LIFO)...");
        for (int i = ejecutados.size() - 1; i >= 0; i--) {
            SagaStep step = ejecutados.get(i);
            try {
                logger.info("Compensando saga step: {}", step.getNombre());
                step.getCompensacion().accept(null);
            } catch (Exception e) {
                logger.error("Error en compensación de step: {}", step.getNombre(), e);
            }
        }
        ejecutados.clear();
    }

    public static class SagaException extends Exception {
        public SagaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
