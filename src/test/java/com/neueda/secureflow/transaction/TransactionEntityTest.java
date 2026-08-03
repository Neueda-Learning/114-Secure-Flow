package com.neueda.secureflow.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionEntityTest {
    @Test
    void storesEveryTransactionValue() {
        Instant time = Instant.parse("2026-08-03T09:30:00Z");
        TransactionEntity transaction = new TransactionEntity(
                "ACC-001", "PAYEE-001", new BigDecimal("150.25"),
                "USD", time, "Invoice payment", time);

        assertNull(transaction.getId());
        assertEquals("ACC-001", transaction.getAccountId());
        assertEquals("PAYEE-001", transaction.getPayeeId());
        assertEquals(new BigDecimal("150.25"), transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals(time, transaction.getTransactionTime());
        assertEquals("Invoice payment", transaction.getDescription());
        assertEquals(time, transaction.getCreatedAt());
    }
}
