package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.Optional;

public interface MonitoringRule {
    Optional<RuleMatch> evaluate(TransactionEntity transaction, RuleContext context);
}
