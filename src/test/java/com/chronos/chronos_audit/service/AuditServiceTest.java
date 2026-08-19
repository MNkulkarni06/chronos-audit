// src/test/java/com/chronos/chronos_audit/service/AuditServiceTest.java
package com.chronos.chronos_audit.service;

import com.chronos.chronos_audit.dto.BankTransactionRequest;
import com.chronos.chronos_audit.dto.EmailMetadataRequest;
import com.chronos.chronos_audit.dto.NetworkLogRequest;
import com.chronos.chronos_audit.dto.SubscriptionDTO;
import com.chronos.chronos_audit.entity.Subscription;
import com.chronos.chronos_audit.repository.AuditLogRepository;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setProviderName("Netflix");
        subscription.setMonthlyAmount(new BigDecimal("15.99"));
        subscription.setStatus("ACTIVE");
        subscription.setLastInteractionTimestamp(LocalDateTime.now().minusDays(40));
    }

    // ---------- calculateLeakRisk ----------

    @Test
    void calculateLeakRisk_flagsLeak_whenNoNetworkAndNoEmailActivity() {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setHasNetworkActivity(false);
        dto.setHasEmailActivity(false);
        dto.setHasRecurringCharge(true);

        Map<String, Object> result = auditService.calculateLeakRisk("1", dto);

        assertEquals("1", result.get("subscriptionId"));
        assertEquals(true, result.get("isLeaking"));
        assertEquals("LEAK", result.get("status"));
    }

    @Test
    void calculateLeakRisk_marksActive_whenEmailActivityPresent() {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setHasNetworkActivity(false);
        dto.setHasEmailActivity(true);
        dto.setHasRecurringCharge(true);

        Map<String, Object> result = auditService.calculateLeakRisk("1", dto);

        assertEquals(false, result.get("isLeaking"));
        assertEquals("ACTIVE", result.get("status"));
    }

    @Test
    void calculateLeakRisk_marksActive_whenNetworkActivityPresent() {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setHasNetworkActivity(true);
        dto.setHasEmailActivity(false);
        dto.setHasRecurringCharge(true);

        Map<String, Object> result = auditService.calculateLeakRisk("1", dto);

        assertEquals(false, result.get("isLeaking"));
        assertEquals("ACTIVE", result.get("status"));
    }

    // ---------- processNetworkLog ----------

    @Test
    void processNetworkLog_updatesSubscription_whenProviderFound() {
        NetworkLogRequest request = new NetworkLogRequest("Netflix", "api-global.netflix.com", LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("Netflix"))
                .thenReturn(Optional.of(subscription));

        String result = auditService.processNetworkLog(request);

        assertTrue(result.contains("Activity recorded"));
        assertEquals("ACTIVE", subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(auditLogRepository).save(any());
    }

    @Test
    void processNetworkLog_returnsWarning_whenProviderNotFound() {
        NetworkLogRequest request = new NetworkLogRequest("UnknownCo", "unknown.com", LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("UnknownCo"))
                .thenReturn(Optional.empty());

        String result = auditService.processNetworkLog(request);

        assertTrue(result.contains("not tracked"));
        verify(subscriptionRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void processNetworkLog_defaultsTimestamp_whenNullProvided() {
        NetworkLogRequest request = new NetworkLogRequest("Netflix", "api-global.netflix.com", null);
        when(subscriptionRepository.findByProviderNameIgnoreCase("Netflix"))
                .thenReturn(Optional.of(subscription));

        auditService.processNetworkLog(request);

        assertNotNull(subscription.getLastInteractionTimestamp());
    }

    // ---------- processEmailMetadata ----------

    @Test
    void processEmailMetadata_updatesSubscription_whenSenderDomainMatchesProvider() {
        EmailMetadataRequest request = new EmailMetadataRequest(
                "no-reply@netflix.com", "Your statement is ready", LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("netflix"))
                .thenReturn(Optional.of(subscription));

        String result = auditService.processEmailMetadata(request);

        assertTrue(result.contains("Inbound notification header parsed"));
        assertEquals("ACTIVE", subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(auditLogRepository).save(any());
    }

    @Test
    void processEmailMetadata_returnsWarning_whenSenderDomainDoesNotMatch() {
        EmailMetadataRequest request = new EmailMetadataRequest(
                "hello@unknownco.com", "Hi", LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("unknownco"))
                .thenReturn(Optional.empty());

        String result = auditService.processEmailMetadata(request);

        assertTrue(result.contains("does not match any active subscription"));
        verify(subscriptionRepository, never()).save(any());
    }

    // ---------- processBankTransaction ----------

    @Test
    void processBankTransaction_updatesSubscription_whenMerchantFound() {
        BankTransactionRequest request = new BankTransactionRequest(
                "Netflix", new BigDecimal("15.99"), LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("Netflix"))
                .thenReturn(Optional.of(subscription));

        String result = auditService.processBankTransaction(request);

        assertTrue(result.contains("Payment charge"));
        assertEquals("ACTIVE", subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(auditLogRepository).save(any());
    }

    @Test
    void processBankTransaction_returnsWarning_whenMerchantNotRegistered() {
        BankTransactionRequest request = new BankTransactionRequest(
                "RandomMerchant", new BigDecimal("5.00"), LocalDateTime.now());
        when(subscriptionRepository.findByProviderNameIgnoreCase("RandomMerchant"))
                .thenReturn(Optional.empty());

        String result = auditService.processBankTransaction(request);

        assertTrue(result.contains("Unregistered recurring merchant"));
        verify(subscriptionRepository, never()).save(any());
    }

    // ---------- evaluateDormantSubscription ----------

    @Test
    void evaluateDormantSubscription_flagsCriticalLeak_andWritesAuditLog() {
        auditService.evaluateDormantSubscription(subscription);

        assertEquals("CRITICAL_LEAK", subscription.getStatus());
        verify(subscriptionRepository).save(subscription);
        verify(auditLogRepository).save(any());
    }

    @Test
    void evaluateDormantSubscription_doesNothing_whenSubscriptionIsNull() {
        auditService.evaluateDormantSubscription(null);

        verify(subscriptionRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }
}