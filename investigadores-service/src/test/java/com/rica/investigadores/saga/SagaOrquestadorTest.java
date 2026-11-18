package com.rica.investigadores.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SagaOrquestadorTest {

    private SagaOrquestador saga;
    private AtomicInteger stepCounter;

    @BeforeEach
    void setUp() {
        saga = new SagaOrquestador();
        stepCounter = new AtomicInteger(0);
    }

    @Test
    void testSagaHappyPath() throws Exception {
        SagaOrquestador.SagaStep step1 = new SagaOrquestador.SagaStep(
            "step-1",
            () -> stepCounter.incrementAndGet(),
            (e) -> stepCounter.decrementAndGet()
        );

        SagaOrquestador.SagaStep step2 = new SagaOrquestador.SagaStep(
            "step-2",
            () -> stepCounter.incrementAndGet(),
            (e) -> stepCounter.decrementAndGet()
        );

        saga.agregarStep(step1);
        saga.agregarStep(step2);

        saga.ejecutar();

        assertEquals(2, stepCounter.get());
    }

    @Test
    void testSagaConFallo() {
        SagaOrquestador.SagaStep step1 = new SagaOrquestador.SagaStep(
            "step-1",
            () -> stepCounter.incrementAndGet(),
            (e) -> stepCounter.decrementAndGet()
        );

        SagaOrquestador.SagaStep stepFallido = new SagaOrquestador.SagaStep(
            "step-fallido",
            () -> {
                throw new RuntimeException("Error intencional");
            },
            (e) -> stepCounter.set(-1)
        );

        saga.agregarStep(step1);
        saga.agregarStep(stepFallido);

        assertThrows(SagaOrquestador.SagaException.class, () -> saga.ejecutar());
        assertEquals(0, stepCounter.get());
    }
}
