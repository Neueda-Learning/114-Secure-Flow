package com.neueda.secureflow.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.neueda.secureflow.config.MonitoringProperties;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RuleControllerTest {
    @Test
    void exposesThreeReadableRuleDefinitions() {
        var properties = new MonitoringProperties(
                new MonitoringProperties.Amount(true, new BigDecimal("10000"), "INR"),
                new MonitoringProperties.Velocity(true, 5, 10),
                new MonitoringProperties.NewPayee(true));

        var rules = new RuleController(properties).list();

        assertThat(rules).extracting(rule -> rule.type()).containsExactly(
                RuleType.AMOUNT_THRESHOLD, RuleType.VELOCITY, RuleType.NEW_PAYEE);
        assertThat(rules.getFirst().parameters()).containsEntry("currency", "INR");
    }
}
