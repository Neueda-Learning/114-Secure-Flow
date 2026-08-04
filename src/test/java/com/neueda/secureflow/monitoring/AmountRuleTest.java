package com.neueda.secureflow.monitoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neueda.secureflow.config.MonitoringProperties;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AmountRuleTest {

    private final AmountRule rule = new AmountRule(new MonitoringProperties(
            new MonitoringProperties.Amount(true, new BigDecimal("10000.00"), "USD"),
            new MonitoringProperties.Velocity(true, 5, 10),
            new MonitoringProperties.NewPayee(true)
    ));

    @Test
    void amountAtThresholdDoesNotTrigger() {
        assertFalse(rule.isTriggered(new BigDecimal("10000.00")));
    }

    @Test
    void amountAboveThresholdTriggers() {
        assertTrue(rule.isTriggered(new BigDecimal("10000.01")));
    }
}
