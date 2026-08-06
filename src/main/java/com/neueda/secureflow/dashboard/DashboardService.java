package com.neueda.secureflow.dashboard;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
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
        long activeAlerts = alerts.countByStatusIn(List.of(
                AlertStatus.OPEN,
                AlertStatus.ACKNOWLEDGED,
                AlertStatus.INVESTIGATING));

        return new DashboardResponse(
                activeAlerts,
                transactions.count(),
                alerts.count(),
                transactions.sumAll());
    }
}
