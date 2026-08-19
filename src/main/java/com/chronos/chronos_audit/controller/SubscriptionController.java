package com.chronos.chronos_audit.controller;

import com.chronos.chronos_audit.dto.*;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import com.chronos.chronos_audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Subscription Audit Controller", description = "Endpoints for telemetry processing and leak assessment")
public class SubscriptionController {

    private final AuditService auditService;

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionController(AuditService auditService, SubscriptionRepository subscriptionRepository) {
        this.auditService = auditService;
        this.subscriptionRepository = subscriptionRepository;
    }
    @PostMapping("/evaluate/{id}")
    @Operation(summary = "Evaluate subscription leak risk based on telemetry payload")
    public ResponseEntity<Map<String, Object>> runAudit(
            @PathVariable String id,
            @Valid @RequestBody SubscriptionDTO telemetryPayload) {

        Map<String, Object> result = auditService.calculateLeakRisk(id, telemetryPayload);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/network-logs")
    @Operation(summary = "Ingest DNS lookup logs to bump interaction timestamp and mark subscription ACTIVE")
    public ResponseEntity<String> ingestNetworkLog(@Valid @RequestBody NetworkLogRequest logRequest) {
        String response = auditService.processNetworkLog(logRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-logs")
    @Operation(summary = "Ingest email header metadata to detect active service usage and bump interaction timestamp")
    public ResponseEntity<String> ingestEmailMetadata(@Valid @RequestBody EmailMetadataRequest emailRequest) {
        String response = auditService.processEmailMetadata(emailRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bank-transactions")
    @Operation(summary = "Ingest financial ledger payment logs to reconcile recurring charges and update timestamps")
    public ResponseEntity<String> ingestBankTransaction(@Valid @RequestBody BankTransactionRequest transactionRequest) {
        String response = auditService.processBankTransaction(transactionRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/leaks")
    public ResponseEntity<List<LeakingSubscriptionDTO>> getCriticalLeaksDashboard() {
        List<LeakingSubscriptionDTO> leaks = subscriptionRepository.findAllCriticalLeaksSummary();
        return ResponseEntity.ok(leaks);
    }
}