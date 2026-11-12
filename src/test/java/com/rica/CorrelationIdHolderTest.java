package com.rica;

import com.rica.observability.CorrelationIdHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationIdHolderTest {

    @AfterEach
    public void tearDown() {
        CorrelationIdHolder.clear();
    }

    @Test
    public void testGenerateCorrelationId() {
        String id1 = CorrelationIdHolder.generateCorrelationId();
        String id2 = CorrelationIdHolder.generateCorrelationId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    public void testSetAndGetCorrelationId() {
        String testId = "TEST-CORR-001";
        CorrelationIdHolder.setCorrelationId(testId);

        assertEquals(testId, CorrelationIdHolder.getCorrelationId());
    }

    @Test
    public void testCorrelationIdPersistence() {
        CorrelationIdHolder.setCorrelationId("PERSIST-001");
        String firstGet = CorrelationIdHolder.getCorrelationId();
        String secondGet = CorrelationIdHolder.getCorrelationId();

        assertEquals(firstGet, secondGet);
    }

    @Test
    public void testClearCorrelationId() {
        CorrelationIdHolder.setCorrelationId("CLEAR-001");
        CorrelationIdHolder.clear();

        // After clear, should generate a new one
        String newId = CorrelationIdHolder.getCorrelationId();
        assertNotEquals("CLEAR-001", newId);
    }
}
