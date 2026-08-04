package com.neueda.secureflow.dashboard;

import com.neueda.secureflow.alert.AlertRepository;
import com.neueda.secureflow.alert.AlertStatus;
import com.neueda.secureflow.transaction.TransactionRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DashboardService {
    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
    private static final List<AlertStatus> ACTIVE_STATUSES = List.of(
            AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED, AlertStatus.INVESTIGATING);

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final Clock clock;

    @Autowired
    public DashboardService(TransactionRepository transactionRepository, AlertRepository alertRepository) {
        this(transactionRepository, alertRepository, Clock.systemUTC());
    }

    DashboardService(TransactionRepository transactionRepository, AlertRepository alertRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        var today = LocalDate.ofInstant(clock.instant(), INDIA_ZONE);
        var from = today.atStartOfDay(INDIA_ZONE).toInstant();
        var to = today.plusDays(1).atStartOfDay(INDIA_ZONE).toInstant();
        return new DashboardSummaryResponse(
                alertRepository.countByStatusIn(ACTIVE_STATUSES),
                transactionRepository.countByTransactionTimeBetween(from, to),
                alertRepository.countByCreatedAtBetween(from, to),
                transactionRepository.sumAmountBetween(from, to));
    }
}
