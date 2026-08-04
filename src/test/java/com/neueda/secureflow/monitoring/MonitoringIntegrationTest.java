package com.neueda.secureflow.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.common.BadRequestException;
import com.neueda.secureflow.transaction.CreateTransactionRequest;
import com.neueda.secureflow.transaction.TransactionRepository;
import com.neueda.secureflow.transaction.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MonitoringIntegrationTest {
    @Autowired private TransactionService service;
    @Autowired private TransactionRepository transactions;
    @Autowired private AlertRepository alerts;

    @BeforeEach
    void cleanDatabase() {
        alerts.deleteAll();
        transactions.deleteAll();
    }

    @Test
    void highAmountAndFirstPayeeCreateRealAlerts() {
        var created = create("ACC-HIGH", "PAYEE-NEW", "10000.01",
                Instant.parse("2026-08-04T10:00:00Z"));

        assertEquals(2, created.generatedAlerts().size());
        assertTrue(created.generatedAlerts().stream()
                .anyMatch(alert -> alert.ruleType() == RuleType.AMOUNT_THRESHOLD));
        assertTrue(created.generatedAlerts().stream()
                .anyMatch(alert -> alert.ruleType() == RuleType.NEW_PAYEE));

        var second = create("ACC-HIGH", "PAYEE-NEW", "10000.00",
                Instant.parse("2026-08-04T10:01:00Z"));
        assertTrue(second.generatedAlerts().stream()
                .noneMatch(alert -> alert.ruleType() == RuleType.AMOUNT_THRESHOLD
                        || alert.ruleType() == RuleType.NEW_PAYEE));
    }

    @Test
    void sixthTransactionCreatesVelocityAlertAndFiltersWork() {
        Instant start = Instant.parse("2026-08-04T11:00:00Z");
        var results = IntStream.range(0, 6)
                .mapToObj(index -> create("ACC-FAST", "PAYEE-FAST", String.valueOf(10 + index),
                        start.plusSeconds(index * 30L))).toList();

        assertTrue(results.get(4).generatedAlerts().stream()
                .noneMatch(alert -> alert.ruleType() == RuleType.VELOCITY));
        assertTrue(results.get(5).generatedAlerts().stream()
                .anyMatch(alert -> alert.ruleType() == RuleType.VELOCITY));

        var page = service.search("FAST", new BigDecimal("12"), new BigDecimal("14"),
                start, start.plusSeconds(150), 0, 20);
        assertEquals(3, page.totalItems());
        assertEquals(3, page.items().size());
        assertThrows(BadRequestException.class, () -> service.search(null,
                new BigDecimal("20"), new BigDecimal("10"), null, null, 0, 20));
    }

    @Test
    void transactionsOutsideTheVelocityWindowAreIgnored() {
        Instant start = Instant.parse("2026-08-04T12:00:00Z");
        IntStream.range(0, 5).forEach(index -> create("ACC-SLOW", "PAYEE-SLOW", "10",
                start.plusSeconds(index * 30L)));

        var later = create("ACC-SLOW", "PAYEE-SLOW", "10", start.plusSeconds(11 * 60L));

        assertTrue(later.generatedAlerts().stream()
                .noneMatch(alert -> alert.ruleType() == RuleType.VELOCITY));
    }

    private com.neueda.secureflow.transaction.TransactionCreatedResponse create(
            String account, String payee, String amount, Instant time) {
        return service.create(new CreateTransactionRequest(account, payee, new BigDecimal(amount),
                "USD", time, "integration test"));
    }
}
