package com.chronos.chronos_audit.controller;

import com.chronos.chronos_audit.dto.SubscriptionDTO;
import com.chronos.chronos_audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class SubscriptionController {

    private final AuditService auditService;

    // Dependency Injection via Constructor
    public SubscriptionController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/evaluate/{id}")
    public ResponseEntity<Map<String, Object>> runAudit(
            @PathVariable String id,
            @RequestBody SubscriptionDTO telemetryPayload) {

        Map<String, Object> result = auditService.calculateLeakRisk(id, telemetryPayload);
        return ResponseEntity.ok(result);
    }
}