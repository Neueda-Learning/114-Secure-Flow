package com.neueda.secureflow.transaction.dto;

import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import java.util.List;

public record TransactionCreatedResponse(
        TransactionResponse transaction,
        List<AlertSummaryResponse> generatedAlerts
) {}
