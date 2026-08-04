package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;

public record RuleDefinitionResponse(String name, AlertSeverity severity, String setting) {
}
