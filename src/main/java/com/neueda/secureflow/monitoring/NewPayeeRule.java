package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class NewPayeeRule implements MonitoringRule {
    private final MonitoringProperties properties;

    public NewPayeeRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        if (!properties.newPayee().enabled() || !context.newPayee()) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(RuleType.NEW_PAYEE, "New payee detected", AlertSeverity.MEDIUM,
                "First payment from " + transaction.getAccountId() + " to " + transaction.getPayeeId(),
                transaction.getAccountId(), List.of(transaction)));
    }
}
