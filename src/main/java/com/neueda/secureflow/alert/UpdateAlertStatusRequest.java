package com.neueda.secureflow.alert;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAlertStatusRequest(@NotNull AlertStatus targetStatus,
                                       @Size(max = 500) String resolutionNotes) {
}
