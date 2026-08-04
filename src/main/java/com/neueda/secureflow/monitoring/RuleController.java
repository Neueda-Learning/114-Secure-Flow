package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.MonitoringProperties;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final MonitoringProperties properties;

    public RuleController(MonitoringProperties properties) { this.properties = properties; }

    @GetMapping
    public List<RuleDefinitionResponse> list() {
        return List.of(
                new RuleDefinitionResponse("High amount", AlertSeverity.HIGH,
                        "More than " + properties.amount().threshold() + " " + properties.amount().currency()),
                new RuleDefinitionResponse("Velocity", AlertSeverity.HIGH,
                        "More than " + properties.velocity().maximumTransactions() + " in "
                                + properties.velocity().windowMinutes() + " minutes"),
                new RuleDefinitionResponse("New payee", AlertSeverity.MEDIUM,
                        "First payment to an account/payee pair"));
    }
}
