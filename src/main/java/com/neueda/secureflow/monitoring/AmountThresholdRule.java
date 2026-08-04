package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AmountThresholdRule implements MonitoringRule {
    private final MonitoringProperties properties;

    public AmountThresholdRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    @Override
    public RuleType type() { return RuleType.AMOUNT_THRESHOLD; }

    @Override
    public boolean enabled() { return properties.amount().enabled(); }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        if (!enabled() || transaction.getAmount().compareTo(properties.amount().threshold()) <= 0) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(type(), "High amount transaction", AlertSeverity.HIGH,
                "Transaction of " + transaction.getCurrency() + " " + transaction.getAmount()
                        + " exceeds the " + properties.amount().threshold() + " threshold",
                transaction.getAccountId(), List.of(transaction)));
    }
}
