package com.neueda.secureflow.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewPayeeRuleTest {
    private final NewPayeeRule rule = new NewPayeeRule(properties());

    @Test
    void firstPaymentTriggersAndSecondDoesNot() {
        TransactionEntity transaction = transaction();
        assertTrue(rule.evaluate(transaction, new RuleContext(true, List.of(transaction))).isPresent());
        assertTrue(rule.evaluate(transaction, new RuleContext(false, List.of(transaction))).isEmpty());
    }

    private MonitoringProperties properties() {
        return new MonitoringProperties(new MonitoringProperties.Amount(true, new BigDecimal("10000"), "INR"),
                new MonitoringProperties.Velocity(true, 5, 10), new MonitoringProperties.NewPayee(true));
    }

    private TransactionEntity transaction() {
        Instant now = Instant.parse("2026-08-04T10:00:00Z");
        return new TransactionEntity("ACC-1", "PAYEE-1", BigDecimal.TEN, "INR", now, null, now);
    }
}
