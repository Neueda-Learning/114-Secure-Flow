package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import com.neueda.secureflow.monitoring.dto.RuleDefinitionResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final MonitoringProperties properties;

    public RuleController(MonitoringProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public List<RuleDefinitionResponse> list() {
        return List.of(
                new RuleDefinitionResponse(RuleType.AMOUNT_THRESHOLD, "High amount transaction",
                        properties.amount().enabled(), AlertSeverity.HIGH,
                        "Alerts when one transaction exceeds the configured amount.",
                        Map.of("threshold", properties.amount().threshold(),
                                "currency", properties.amount().currency())),
                new RuleDefinitionResponse(RuleType.VELOCITY, "Rapid transaction velocity",
                        properties.velocity().enabled(), AlertSeverity.HIGH,
                        "Alerts when too many transactions occur for one account in a rolling window.",
                        Map.of("maximumTransactions", properties.velocity().maximumTransactions(),
                                "windowMinutes", properties.velocity().windowMinutes())),
                new RuleDefinitionResponse(RuleType.NEW_PAYEE, "New payee detected",
                        properties.newPayee().enabled(), AlertSeverity.MEDIUM,
                        "Alerts on the first transaction from an account to a payee.", Map.of())
        );
    }
}
