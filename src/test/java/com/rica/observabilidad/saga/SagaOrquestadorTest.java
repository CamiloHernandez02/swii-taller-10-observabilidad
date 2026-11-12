package com.rica.observabilidad.saga;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SagaOrquestadorTest {
    @Test
    public void testSagaExitosa() throws SagaOrquestador.SagaFallidaException {
        SagaOrquestador saga = new SagaOrquestador();
        java.util.List<String> pasos = new java.util.ArrayList<>();
        
        saga.agregarPaso("Paso 1", 
            () -> pasos.add("paso1"),
            () -> pasos.add("compensacion1")
        );
        
        saga.agregarPaso("Paso 2",
            () -> pasos.add("paso2"),
            () -> pasos.add("compensacion2")
        );
        
        saga.ejecutarSaga();
        
        assertTrue(pasos.contains("paso1"));
        assertTrue(pasos.contains("paso2"));
        assertFalse(pasos.contains("compensacion1"));
        assertFalse(pasos.contains("compensacion2"));
    }

    @Test
    public void testSagaConFallo() {
        SagaOrquestador saga = new SagaOrquestador();
        java.util.List<String> pasos = new java.util.ArrayList<>();
        
        saga.agregarPaso("Paso 1",
            () -> pasos.add("paso1"),
            () -> pasos.add("compensacion1")
        );
        
        saga.agregarPaso("Paso 2",
            () -> {
                pasos.add("paso2");
                throw new RuntimeException("Error en paso 2");
            },
            () -> pasos.add("compensacion2")
        );
        
        assertThrows(SagaOrquestador.SagaFallidaException.class, saga::ejecutarSaga);
        
        assertTrue(pasos.contains("paso1"));
        assertTrue(pasos.contains("paso2"));
        assertTrue(pasos.contains("compensacion1"));
    }
}
