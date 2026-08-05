package com.neueda.secureflow.monitoring;

import com.neueda.secureflow.alert.AlertSeverity;
import com.neueda.secureflow.config.RulesConfig;
import com.neueda.secureflow.monitoring.dto.RuleResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final RulesConfig rules;

    public RuleController(RulesConfig rules) {
        this.rules = rules;
    }

    @GetMapping
    public List<RuleResponse> getRules() {
        return List.of(
                new RuleResponse(
                        RuleType.AMOUNT_THRESHOLD,
                        "High amount transaction",
                        true,
                        AlertSeverity.HIGH,
                        "One transaction is above the amount limit.",
                        Map.of("threshold", rules.amountLimit(), "currency", rules.currency())),
                new RuleResponse(
                        RuleType.VELOCITY,
                        "Rapid transaction velocity",
                        true,
                        AlertSeverity.HIGH,
                        "Too many transactions happen in a short time.",
                        Map.of(
                                "maximumTransactions", rules.maxTransactions(),
                                "windowMinutes", rules.windowMinutes())),
                new RuleResponse(
                        RuleType.NEW_PAYEE,
                        "New payee detected",
                        true,
                        AlertSeverity.MEDIUM,
                        "An account sends money to a payee for the first time.",
                        Map.of()));
    }
}
