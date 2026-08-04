package com.neueda.secureflow.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class VelocityRuleTest {
    private final VelocityRule rule = new VelocityRule(new MonitoringProperties(
            new MonitoringProperties.Amount(true, new BigDecimal("10000"), "USD"),
            new MonitoringProperties.Velocity(true, 5, 10), new MonitoringProperties.NewPayee(true)));

    @Test
    void fifthDoesNotTriggerButSixthDoes() {
        TransactionEntity transaction = transaction();
        assertTrue(rule.evaluate(transaction,
                new RuleContext(false, Collections.nCopies(5, transaction))).isEmpty());
        assertTrue(rule.evaluate(transaction,
                new RuleContext(false, Collections.nCopies(6, transaction))).isPresent());
    }

    private TransactionEntity transaction() {
        Instant now = Instant.parse("2026-08-04T10:00:00Z");
        return new TransactionEntity("ACC-1", "PAYEE-1", BigDecimal.TEN, "USD", now, null, now);
    }
}
