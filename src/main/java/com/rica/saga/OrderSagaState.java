package com.rica.saga;

public class OrderSagaState {
    public enum State {
        PENDING,
        PAYMENT_PROCESSED,
        INVENTORY_RESERVED,
        SHIPMENT_SCHEDULED,
        COMPLETED,
        FAILED
    }

    private String orderId;
    private State currentState;
    private String failureReason;

    public OrderSagaState(String orderId) {
        this.orderId = orderId;
        this.currentState = State.PENDING;
    }

    public String getOrderId() {
        return orderId;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean canTransitionTo(State nextState) {
        return currentState.ordinal() < nextState.ordinal();
    }

    @Override
    public String toString() {
        return "OrderSagaState{" +
                "orderId='" + orderId + '\'' +
                ", currentState=" + currentState +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
