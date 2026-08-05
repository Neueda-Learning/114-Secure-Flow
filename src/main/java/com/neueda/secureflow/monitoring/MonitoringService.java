package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertEntity;
import com.neueda.secureflow.alert.AlertService;
import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.RulesConfig;
import com.neueda.secureflow.transaction.TransactionEntity;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {
    private final TransactionRepository transactionRepository;
    private final AlertService alertService;
    private final RulesConfig rules;

    public MonitoringService(
            TransactionRepository transactionRepository,
            AlertService alertService,
            RulesConfig rules) {
        this.transactionRepository = transactionRepository;
        this.alertService = alertService;
        this.rules = rules;
    }

    public List<AlertEntity> check(TransactionEntity transaction, boolean isNewPayee) {
        List<AlertEntity> alerts = new ArrayList<>();

        if (transaction.getAmount().compareTo(rules.amountLimit()) > 0) {
            alerts.add(alertService.create(
                    RuleType.AMOUNT_THRESHOLD,
                    "High amount transaction",
                    AlertSeverity.HIGH,
                    "Transaction of " + transaction.getCurrency() + " "
                            + transaction.getAmount() + " exceeds " + rules.amountLimit(),
                    transaction.getAccountId(),
                    List.of(transaction)));
        }

        var windowStart = transaction.getTransactionTime()
                .minus(rules.windowMinutes(), ChronoUnit.MINUTES);
        var windowEnd = transaction.getTransactionTime().plus(1, ChronoUnit.SECONDS);
        var recentTransactions =
                transactionRepository.findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
                        transaction.getAccountId(), windowStart, windowEnd);

        if (recentTransactions.size() > rules.maxTransactions()) {
            alerts.add(alertService.create(
                    RuleType.VELOCITY,
                    "Rapid transaction velocity",
                    AlertSeverity.HIGH,
                    recentTransactions.size() + " transactions were recorded within "
                            + rules.windowMinutes() + " minutes",
                    transaction.getAccountId(),
                    recentTransactions));
        }

        if (isNewPayee) {
            alerts.add(alertService.create(
                    RuleType.NEW_PAYEE,
                    "New payee detected",
                    AlertSeverity.MEDIUM,
                    transaction.getPayeeId() + " is a new payee for "
                            + transaction.getAccountId(),
                    transaction.getAccountId(),
                    List.of(transaction)));
        }

        return alerts;
    }
}
