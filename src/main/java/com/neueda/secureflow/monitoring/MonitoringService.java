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
    private final TransactionRepository transactions;
    private final AlertService alerts;
    private final MonitoringProperties properties;

    public MonitoringService(List<MonitoringRule> rules, TransactionRepository transactions,
                             AlertService alerts, MonitoringProperties properties) {
        this.rules = rules;
        this.transactions = transactions;
        this.alerts = alerts;
        this.properties = properties;
    }

    public List<AlertEntity> evaluate(TransactionEntity transaction, boolean newPayee) {
        var from = transaction.getTransactionTime()
                .minus(properties.velocity().windowMinutes(), ChronoUnit.MINUTES);
        var recent = transactions.findByAccountIdAndTransactionTimeBetweenOrderByTransactionTimeAsc(
                transaction.getAccountId(), from, transaction.getTransactionTime());
        var context = new RuleContext(newPayee, recent);
        List<AlertEntity> created = new ArrayList<>();
        for (MonitoringRule rule : rules) {
            rule.evaluate(transaction, context).map(alerts::create).ifPresent(created::add);
        }
        return created;
    }
}
