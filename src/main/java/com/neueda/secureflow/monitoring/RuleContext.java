package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.transaction.TransactionEntity;
import java.util.List;

public record RuleContext(boolean newPayee, List<TransactionEntity> recentTransactions) {
}
