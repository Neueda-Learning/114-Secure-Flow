package com.neueda.secureflow.alert;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) { this.service = service; }

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) AlertStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable long id) { return service.get(id); }

    @PatchMapping("/{id}/status")
    public AlertResponse transition(@PathVariable long id,
                                    @Valid @RequestBody UpdateAlertStatusRequest request) {
        return service.transition(id, request.targetStatus(), request.resolutionNotes());
    }
}
