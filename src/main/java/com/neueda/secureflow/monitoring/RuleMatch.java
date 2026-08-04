package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;

public record RuleMatch(RuleType ruleType, String ruleName, AlertSeverity severity,
                        String message, String accountId, List<TransactionEntity> transactions) {
}
