package com.neueda.secureflow.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MonitoringRulesTest {
    private MonitoringProperties properties;
    private TransactionEntity transaction;

    @BeforeEach
    void setUp() {
        properties = new MonitoringProperties(
                new MonitoringProperties.Amount(true, new BigDecimal("10000.00"), "INR"),
                new MonitoringProperties.Velocity(true, 5, 10),
                new MonitoringProperties.NewPayee(true));
        transaction = transaction("10000.00");
    }

    @Test
    void amountRuleTriggersOnlyAboveThreshold() {
        var rule = new AmountThresholdRule(properties);

        assertThat(rule.type()).isEqualTo(RuleType.AMOUNT_THRESHOLD);
        assertThat(rule.enabled()).isTrue();
        assertThat(rule.evaluate(transaction, context(false, 1))).isEmpty();
        assertThat(rule.evaluate(transaction("10000.01"), context(false, 1)))
                .get().extracting(RuleMatch::severity).isEqualTo(com.neueda.secureflow.alert.AlertSeverity.HIGH);
    }

    @Test
    void velocityRuleTriggersOnSixthTransaction() {
        var rule = new VelocityRule(properties);

        assertThat(rule.type()).isEqualTo(RuleType.VELOCITY);
        assertThat(rule.evaluate(transaction, context(false, 5))).isEmpty();
        assertThat(rule.evaluate(transaction, context(false, 6)))
                .get().satisfies(match -> assertThat(match.transactions()).hasSize(6));
    }

    @Test
    void newPayeeRuleTriggersOnlyForFirstUse() {
        var rule = new NewPayeeRule(properties);

        assertThat(rule.type()).isEqualTo(RuleType.NEW_PAYEE);
        assertThat(rule.evaluate(transaction, context(false, 1))).isEmpty();
        assertThat(rule.evaluate(transaction, context(true, 1)))
                .get().extracting(RuleMatch::ruleType).isEqualTo(RuleType.NEW_PAYEE);
    }

    @Test
    void disabledRulesNeverTrigger() {
        var disabled = new MonitoringProperties(
                new MonitoringProperties.Amount(false, BigDecimal.ZERO, "INR"),
                new MonitoringProperties.Velocity(false, 0, 10),
                new MonitoringProperties.NewPayee(false));

        assertThat(new AmountThresholdRule(disabled).evaluate(transaction, context(true, 6))).isEmpty();
        assertThat(new VelocityRule(disabled).evaluate(transaction, context(true, 6))).isEmpty();
        assertThat(new NewPayeeRule(disabled).evaluate(transaction, context(true, 6))).isEmpty();
    }

    private RuleContext context(boolean newPayee, int count) {
        return new RuleContext(newPayee, java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> transaction).toList());
    }

    private TransactionEntity transaction(String amount) {
        return new TransactionEntity("ACC-1", "PAYEE-1", new BigDecimal(amount), "INR",
                Instant.parse("2026-08-02T10:00:00Z"), "Test", Instant.parse("2026-08-02T10:00:00Z"));
    }
}
