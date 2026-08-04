package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertStatus;
import java.time.Instant;

public record AlertHistoryResponse(
        AlertStatus previousStatus,
        AlertStatus newStatus,
        Instant changedAt,
        String note
) {}
