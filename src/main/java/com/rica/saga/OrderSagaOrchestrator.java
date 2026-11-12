package com.rica.saga;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderSagaOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    private final Map<String, OrderSagaState> sagaStates = new ConcurrentHashMap<>();

    public void startOrderSaga(String orderId) {
        OrderSagaState state = new OrderSagaState(orderId);
        sagaStates.put(orderId, state);
        log.info("Order Saga started for orderId: {}", orderId);
        
        // Step 1: Process Payment
        processPayment(orderId);
    }

    private void processPayment(String orderId) {
        OrderSagaState state = sagaStates.get(orderId);
        try {
            log.info("Processing payment for order: {}", orderId);
            // Call payment service
            state.setCurrentState(OrderSagaState.State.PAYMENT_PROCESSED);
            reserveInventory(orderId);
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", orderId, e);
            compensatePayment(orderId);
        }
    }

    private void reserveInventory(String orderId) {
        OrderSagaState state = sagaStates.get(orderId);
        try {
            log.info("Reserving inventory for order: {}", orderId);
            // Call inventory service
            state.setCurrentState(OrderSagaState.State.INVENTORY_RESERVED);
            scheduleShipment(orderId);
        } catch (Exception e) {
            log.error("Inventory reservation failed for order: {}", orderId, e);
            compensatePayment(orderId);
            compensateInventory(orderId);
        }
    }

    private void scheduleShipment(String orderId) {
        OrderSagaState state = sagaStates.get(orderId);
        try {
            log.info("Scheduling shipment for order: {}", orderId);
            // Call shipment service
            state.setCurrentState(OrderSagaState.State.SHIPMENT_SCHEDULED);
            state.setCurrentState(OrderSagaState.State.COMPLETED);
            log.info("Order saga completed successfully: {}", orderId);
        } catch (Exception e) {
            log.error("Shipment scheduling failed for order: {}", orderId, e);
            compensatePayment(orderId);
            compensateInventory(orderId);
            compensateShipment(orderId);
        }
    }

    private void compensatePayment(String orderId) {
        log.warn("Compensating payment for order: {}", orderId);
        OrderSagaState state = sagaStates.get(orderId);
        state.setFailureReason("Payment compensation initiated");
    }

    private void compensateInventory(String orderId) {
        log.warn("Compensating inventory for order: {}", orderId);
        OrderSagaState state = sagaStates.get(orderId);
        state.setFailureReason("Inventory compensation initiated");
    }

    private void compensateShipment(String orderId) {
        log.warn("Compensating shipment for order: {}", orderId);
        OrderSagaState state = sagaStates.get(orderId);
        state.setFailureReason("Shipment compensation initiated");
        state.setCurrentState(OrderSagaState.State.FAILED);
    }

    public OrderSagaState getOrderSagaState(String orderId) {
        return sagaStates.get(orderId);
    }

    public List<OrderSagaState> getAllSagaStates() {
        return new ArrayList<>(sagaStates.values());
    }
}
