package com.neueda.secureflow.dashboard;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.util.Set;
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
        long active = alertList.stream().filter(alert -> Set.of(AlertStatus.OPEN,
                AlertStatus.ACKNOWLEDGED, AlertStatus.INVESTIGATING).contains(alert.getStatus())).count();
        BigDecimal volume = transactionList.stream().map(item -> item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardSummaryResponse(active, transactionList.size(), alertList.size(), volume);
    }
}
