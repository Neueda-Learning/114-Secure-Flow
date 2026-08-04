package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class VelocityRule implements MonitoringRule {
    private final MonitoringProperties properties;

    public VelocityRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    @Override
    public RuleType type() { return RuleType.VELOCITY; }

    @Override
    public boolean enabled() { return properties.velocity().enabled(); }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        int count = context.recentTransactions().size();
        if (!enabled() || count <= properties.velocity().maximumTransactions()) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(type(), "Rapid transaction velocity", AlertSeverity.HIGH,
                count + " transactions were recorded for " + transaction.getAccountId() + " within "
                        + properties.velocity().windowMinutes() + " minutes",
                transaction.getAccountId(), context.recentTransactions()));
    }
}
