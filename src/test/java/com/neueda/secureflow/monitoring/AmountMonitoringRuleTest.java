package com.neueda.secureflow.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AmountMonitoringRuleTest {
    private final MonitoringProperties properties = new MonitoringProperties(
            new MonitoringProperties.Amount(true, new BigDecimal("10000.00"), "USD"),
            new MonitoringProperties.Velocity(true, 5, 10),
            new MonitoringProperties.NewPayee(true));
    private final AmountMonitoringRule rule = new AmountMonitoringRule(new AmountRule(properties));

    @Test
    void convertsSriramsMatchIntoAHighAlert() {
        Instant now = Instant.parse("2026-08-04T10:00:00Z");
        TransactionEntity transaction = new TransactionEntity("ACC-1", "PAYEE-1",
                new BigDecimal("10000.01"), "USD", now, null, now);

        RuleMatch match = rule.evaluate(transaction, new RuleContext(false, List.of(transaction))).orElseThrow();

        assertEquals(RuleType.AMOUNT_THRESHOLD, match.ruleType());
        assertEquals(AlertSeverity.HIGH, match.severity());
        assertTrue(match.transactions().contains(transaction));
    }
}
