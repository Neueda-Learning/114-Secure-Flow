package com.neueda.secureflow.monitoring.dto;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.monitoring.RuleType;
import java.util.Map;

public record RuleResponse(
        RuleType type,
        String name,
        boolean enabled,
        AlertSeverity severity,
        String description,
        Map<String, Object> parameters
) {}
