package com.neueda.secureflow.dashboard;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final TransactionRepository transactions;
    private final AlertRepository alerts;

    public DashboardController(TransactionRepository transactions, AlertRepository alerts) {
        this.transactions = transactions;
        this.alerts = alerts;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        var transactionList = transactions.findAll();
        var alertList = alerts.findAll();
        var today = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        long active = alertList.stream().filter(alert -> Set.of(AlertStatus.OPEN,
                AlertStatus.ACKNOWLEDGED, AlertStatus.INVESTIGATING).contains(alert.getStatus())).count();
        var todaysTransactions = transactionList.stream()
                .filter(item -> !item.getCreatedAt().isBefore(today)).toList();
        long todaysAlerts = alertList.stream().filter(item -> !item.getCreatedAt().isBefore(today)).count();
        BigDecimal volume = todaysTransactions.stream().map(item -> item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardSummaryResponse(active, todaysTransactions.size(), todaysAlerts, volume);
    }
}
