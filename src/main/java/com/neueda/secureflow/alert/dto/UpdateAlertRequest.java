package com.neueda.secureflow.alert.dto;

import com.neueda.secureflow.alert.AlertStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAlertRequest(
        @NotNull AlertStatus targetStatus,
        @Size(max = 500) String resolutionNotes
) {}
