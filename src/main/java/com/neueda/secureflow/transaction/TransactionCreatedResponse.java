package com.neueda.secureflow.transaction;

import com.neueda.secureflow.alert.AlertResponse;
import java.util.List;

public record TransactionCreatedResponse(TransactionResponse transaction,
                                         List<AlertResponse> generatedAlerts) {
}
