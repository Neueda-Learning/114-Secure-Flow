package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class VelocityRule implements MonitoringRule {
    private final MonitoringProperties properties;

    public VelocityRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        int count = context.recentTransactions().size();
        if (!properties.velocity().enabled() || count <= properties.velocity().maximumTransactions()) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(RuleType.VELOCITY, "Rapid transaction velocity", AlertSeverity.HIGH,
                count + " transactions in " + properties.velocity().windowMinutes() + " minutes",
                transaction.getAccountId(), context.recentTransactions()));
    }
}
