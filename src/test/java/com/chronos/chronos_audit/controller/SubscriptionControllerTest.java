// src/test/java/com/chronos/chronos_audit/controller/SubscriptionControllerTest.java
package com.chronos.chronos_audit.controller;

import com.chronos.chronos_audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditService auditService;

    // ---------- POST /api/audit/evaluate/{id} ----------

    @Test
    void evaluate_returnsLeakResult_onValidPayload() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("hasNetworkActivity", false);
        body.put("hasEmailActivity", false);
        body.put("hasRecurringCharge", true);

        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("subscriptionId", "1");
        serviceResponse.put("isLeaking", true);
        serviceResponse.put("status", "LEAK");

        when(auditService.calculateLeakRisk(anyString(), any())).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/audit/evaluate/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value("1"))
                .andExpect(jsonPath("$.isLeaking").value(true))
                .andExpect(jsonPath("$.status").value("LEAK"));
    }

    // ---------- POST /api/audit/network-logs ----------

    @Test
    void ingestNetworkLog_returnsOk_onValidPayload() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("providerName", "Netflix");
        body.put("domain", "api-global.netflix.com");
        body.put("timestamp", "2026-08-01T14:22:00");

        when(auditService.processNetworkLog(any()))
                .thenReturn("✅ [DNS INGRESS] Activity recorded for Netflix.");

        mockMvc.perform(post("/api/audit/network-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void ingestNetworkLog_returns400_whenProviderNameMissing() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("domain", "api-global.netflix.com");
        // providerName intentionally omitted -> should fail @NotBlank

        mockMvc.perform(post("/api/audit/network-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/audit/email-logs ----------

    @Test
    void ingestEmailMetadata_returnsOk_onValidPayload() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("senderEmail", "no-reply@netflix.com");
        body.put("subject", "Your statement is ready");
        body.put("receivedTimestamp", "2026-08-01T09:00:00");

        when(auditService.processEmailMetadata(any()))
                .thenReturn("✅ [EMAIL INGRESS] Parsed.");

        mockMvc.perform(post("/api/audit/email-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void ingestEmailMetadata_returns400_whenSubjectMissing() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("senderEmail", "no-reply@netflix.com");
        // subject intentionally omitted -> should fail @NotBlank

        mockMvc.perform(post("/api/audit/email-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/audit/bank-transactions ----------

    @Test
    void ingestBankTransaction_returnsOk_onValidPayload() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("merchantName", "Netflix");
        body.put("amount", 15.99);
        body.put("transactionTimestamp", "2026-08-01T00:00:00");

        when(auditService.processBankTransaction(any()))
                .thenReturn("✅ [BANK LEDGER INGRESS] Matched.");

        mockMvc.perform(post("/api/audit/bank-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void ingestBankTransaction_returns400_whenAmountMissing() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("merchantName", "Netflix");
        // amount intentionally omitted -> should fail @NotNull

        mockMvc.perform(post("/api/audit/bank-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}