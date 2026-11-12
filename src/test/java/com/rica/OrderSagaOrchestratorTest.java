package com.rica;

import com.rica.saga.OrderSagaOrchestrator;
import com.rica.saga.OrderSagaState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderSagaOrchestratorTest {

    private OrderSagaOrchestrator sagaOrchestrator;

    @BeforeEach
    public void setUp() {
        sagaOrchestrator = new OrderSagaOrchestrator();
    }

    @Test
    public void testStartOrderSaga() {
        String orderId = "ORD-001";
        sagaOrchestrator.startOrderSaga(orderId);

        OrderSagaState state = sagaOrchestrator.getOrderSagaState(orderId);
        assertNotNull(state);
        assertEquals(orderId, state.getOrderId());
    }

    @Test
    public void testSagaStateTransitions() {
        String orderId = "ORD-002";
        OrderSagaState state = new OrderSagaState(orderId);

        assertTrue(state.canTransitionTo(OrderSagaState.State.PAYMENT_PROCESSED));
        state.setCurrentState(OrderSagaState.State.PAYMENT_PROCESSED);

        assertTrue(state.canTransitionTo(OrderSagaState.State.INVENTORY_RESERVED));
    }

    @Test
    public void testSagaFailureHandling() {
        String orderId = "ORD-003";
        OrderSagaState state = new OrderSagaState(orderId);
        
        state.setCurrentState(OrderSagaState.State.FAILED);
        state.setFailureReason("Payment processing failed");

        assertEquals(OrderSagaState.State.FAILED, state.getCurrentState());
        assertEquals("Payment processing failed", state.getFailureReason());
    }

    @Test
    public void testGetAllSagaStates() {
        sagaOrchestrator.startOrderSaga("ORD-004");
        sagaOrchestrator.startOrderSaga("ORD-005");

        var states = sagaOrchestrator.getAllSagaStates();
        assertTrue(states.size() >= 2);
    }
}
