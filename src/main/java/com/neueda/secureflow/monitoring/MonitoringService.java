package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertEntity;
import com.neueda.secureflow.alert.AlertService;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.transaction.TransactionEntity;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {
    private final List<MonitoringRule> rules;
    private final TransactionRepository transactionRepository;
    private final AlertService alertService;
    private final MonitoringProperties properties;

    public MonitoringService(List<MonitoringRule> rules, TransactionRepository transactionRepository,
                             AlertService alertService, MonitoringProperties properties) {
        this.rules = rules;
        this.transactionRepository = transactionRepository;
        this.alertService = alertService;
        this.properties = properties;
    }

    public List<AlertEntity> evaluate(TransactionEntity transaction, boolean newPayee) {
        var windowStart = transaction.getTransactionTime()
                .minus(properties.velocity().windowMinutes(), ChronoUnit.MINUTES);
        var recent = transactionRepository.findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
                transaction.getAccountId(), windowStart, transaction.getTransactionTime());
        var context = new RuleContext(newPayee, recent);
        List<AlertEntity> alerts = new ArrayList<>();
        for (MonitoringRule rule : rules) {
            rule.evaluate(transaction, context).map(alertService::create).ifPresent(alerts::add);
        }
        return alerts;
    }
}
