package com.neueda.secureflow.alert;

import java.time.Instant;

public record AlertHistoryResponse(AlertStatus previousStatus, AlertStatus newStatus,
                                   Instant changedAt, String note) {
}
