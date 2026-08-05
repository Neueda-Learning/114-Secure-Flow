package com.neueda.secureflow.dashboard;

import java.math.BigDecimal;

public record DashboardResponse(
        long activeAlertCount,
        long transactionCount,
        long alertCount,
        BigDecimal transactionVolume
) {}
