package com.neueda.secureflow.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(long activeAlerts, long transactions,
                                       long alerts, BigDecimal transactionVolume) {
}
