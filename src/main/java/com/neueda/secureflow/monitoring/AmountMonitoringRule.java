package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AmountMonitoringRule implements MonitoringRule {
    private final AmountRule amountRule;

    public AmountMonitoringRule(AmountRule amountRule) {
        this.amountRule = amountRule;
    }

    @Override
    public Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context) {
        if (!amountRule.isTriggered(transaction.getAmount())) {
            return Optional.empty();
        }
        return Optional.of(new RuleMatch(RuleType.AMOUNT_THRESHOLD, "High amount",
                AlertSeverity.HIGH, "Transaction amount is above the configured threshold",
                transaction.getAccountId(), List.of(transaction)));
    }
}
