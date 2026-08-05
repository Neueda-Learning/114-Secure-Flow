package com.neueda.secureflow.dashboard;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    private final TransactionRepository transactions;
    private final AlertRepository alerts;

    public DashboardService(
            TransactionRepository transactions,
            AlertRepository alerts) {
        this.transactions = transactions;
        this.alerts = alerts;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {
        LocalDate todayInIndia = LocalDate.now(INDIA);
        Instant start = todayInIndia.atStartOfDay(INDIA).toInstant();
        Instant end = todayInIndia.plusDays(1).atStartOfDay(INDIA).toInstant();

        long activeAlerts = alerts.countByStatusIn(List.of(
                AlertStatus.OPEN,
                AlertStatus.ACKNOWLEDGED,
                AlertStatus.INVESTIGATING));

        return new DashboardResponse(
                activeAlerts,
                transactions.countByTransactionTimeBetween(start, end),
                alerts.countByCreatedAtBetween(start, end),
                transactions.sumBetween(start, end));
    }
}
