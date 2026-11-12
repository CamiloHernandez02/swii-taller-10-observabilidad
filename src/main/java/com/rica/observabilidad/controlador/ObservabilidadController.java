package com.rica.observabilidad.controlador;

import com.rica.observabilidad.metricas.MetricasService;
import com.rica.observabilidad.trazabilidad.TrazabilidadService;
import com.rica.observabilidad.saga.SagaOrquestador;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/observabilidad")
public class ObservabilidadController {
    private final MetricasService metricas;
    private final TrazabilidadService trazabilidad;
    private final SagaOrquestador saga;

    public ObservabilidadController(MetricasService metricas, TrazabilidadService trazabilidad, 
                                  SagaOrquestador saga) {
        this.metricas = metricas;
        this.trazabilidad = trazabilidad;
        this.saga = saga;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/operacion")
    public String ejecutarOperacion(@RequestParam String nombre) {
        return metricas.medirTiempo(nombre, () -> {
            trazabilidad.rastrearOperacion(nombre, () -> {
                try {
                    Thread.sleep(100); // Simular trabajo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return "Operación " + nombre + " completada";
        });
    }

    @PostMapping("/saga")
    public String ejecutarSaga() throws SagaOrquestador.SagaFallidaException {
        SagaOrquestador sagaTemp = new SagaOrquestador();
        
        sagaTemp.agregarPaso(
            "Paso 1",
            () -> System.out.println("Ejecutando paso 1"),
            () -> System.out.println("Compensando paso 1")
        );
        
        sagaTemp.agregarPaso(
            "Paso 2",
            () -> System.out.println("Ejecutando paso 2"),
            () -> System.out.println("Compensando paso 2")
        );
        
        sagaTemp.ejecutarSaga();
        return "Saga ejecutada exitosamente";
    }
}
