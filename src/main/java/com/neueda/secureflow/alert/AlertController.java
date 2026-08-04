package com.neueda.secureflow.alert;

import com.neueda.secureflow.alert.dto.AlertDetailResponse;
import com.neueda.secureflow.alert.dto.AlertSummaryResponse;
import com.neueda.secureflow.alert.dto.UpdateAlertStatusRequest;
import com.neueda.secureflow.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/alerts")
@Validated
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AlertSummaryResponse> search(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(status, severity, page, size);
    }

    @GetMapping("/{id}")
    public AlertDetailResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public AlertDetailResponse transition(@PathVariable long id,
                                          @Valid @RequestBody UpdateAlertStatusRequest request) {
        return service.transition(id, request.targetStatus(), request.resolutionNotes());
    }
}
