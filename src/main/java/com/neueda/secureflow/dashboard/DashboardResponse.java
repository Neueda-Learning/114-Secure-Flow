package com.neueda.secureflow.dashboard;

import java.math.BigDecimal;

public record DashboardResponse(
        long activeAlertCount,
        long transactionCountToday,
        long alertsToday,
        BigDecimal transactionVolumeToday
) {}
