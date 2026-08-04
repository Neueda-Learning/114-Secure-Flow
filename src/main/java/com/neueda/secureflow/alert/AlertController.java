package com.neueda.secureflow.alert;

import jakarta.validation.Valid;
import com.neueda.secureflow.common.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@Validated
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) { this.service = service; }

    @GetMapping
    public PageResponse<AlertResponse> list(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(status, severity, page, size);
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable long id) { return service.get(id); }

    @PatchMapping("/{id}/status")
    public AlertResponse transition(@PathVariable long id,
                                    @Valid @RequestBody UpdateAlertStatusRequest request) {
        return service.transition(id, request.targetStatus(), request.resolutionNotes());
    }
}
