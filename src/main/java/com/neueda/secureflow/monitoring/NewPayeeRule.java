package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class NewPayeeRule implements MonitoringRule {
    private final MonitoringProperties properties;

    public NewPayeeRule(MonitoringProperties properties) {
        this.properties = properties;
    }

    @Override
    public RuleType type() { return RuleType.NEW_PAYEE; }

    @Override
    public boolean enabled() { return properties.newPayee().enabled(); }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        if (!enabled() || !context.newPayee()) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(type(), "New payee detected", AlertSeverity.MEDIUM,
                transaction.getPayeeId() + " has not previously received a transaction from "
                        + transaction.getAccountId(), transaction.getAccountId(), List.of(transaction)));
    }
}
