package com.chronos.chronos_audit.service;

import com.chronos.chronos_audit.dto.BankTransactionRequest;
import com.chronos.chronos_audit.dto.EmailMetadataRequest;
import com.chronos.chronos_audit.dto.NetworkLogRequest;
import com.chronos.chronos_audit.dto.SubscriptionDTO;
import com.chronos.chronos_audit.entity.AuditLog;
import com.chronos.chronos_audit.entity.Subscription;
import com.chronos.chronos_audit.repository.AuditLogRepository;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuditService {

    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;

    public AuditService(SubscriptionRepository subscriptionRepository, AuditLogRepository auditLogRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Map<String, Object> calculateLeakRisk(String id, SubscriptionDTO telemetryPayload) {
        Map<String, Object> result = new HashMap<>();
        boolean isLeaking = !telemetryPayload.isHasNetworkActivity() && !telemetryPayload.isHasEmailActivity();
        result.put("subscriptionId", id);
        result.put("isLeaking", isLeaking);
        result.put("status", isLeaking ? "LEAK" : "ACTIVE");
        return result;
    }

    @Transactional
    public String processNetworkLog(NetworkLogRequest logRequest) {
        String provider = logRequest.getProviderName();
        String domain = logRequest.getDomain();
        LocalDateTime interactionTime = logRequest.getTimestamp() != null
                ? logRequest.getTimestamp()
                : LocalDateTime.now();

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByProviderNameIgnoreCase(provider);

        if (optionalSubscription.isPresent()) {
            Subscription subscription = optionalSubscription.get();
            subscription.setLastInteractionTimestamp(interactionTime);
            subscription.setStatus("ACTIVE");
            subscriptionRepository.save(subscription);

            auditLogRepository.save(new AuditLog(subscription, "DNS", "Domain lookup: " + domain));

            return String.format("✅ [DNS INGRESS] Activity recorded for %s (%s). Status reset to ACTIVE.", provider, domain);
        } else {
            return String.format("⚠️ [DNS INGRESS] Provider '%s' not tracked in system.", provider);
        }
    }

    @Transactional
    public String processEmailMetadata(EmailMetadataRequest emailRequest) {
        String sender = emailRequest.getSenderEmail();
        String providerDomain = sender.contains("@") ? sender.substring(sender.indexOf("@") + 1) : sender;
        String providerKeyword = providerDomain.split("\\.")[0];

        LocalDateTime interactionTime = emailRequest.getReceivedTimestamp() != null
                ? emailRequest.getReceivedTimestamp()
                : LocalDateTime.now();

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByProviderNameIgnoreCase(providerKeyword);

        if (optionalSubscription.isPresent()) {
            Subscription subscription = optionalSubscription.get();
            subscription.setLastInteractionTimestamp(interactionTime);
            subscription.setStatus("ACTIVE");
            subscriptionRepository.save(subscription);

            auditLogRepository.save(new AuditLog(subscription, "EMAIL", "Header parsed from: " + sender));

            return String.format("✅ [EMAIL INGRESS] Inbound notification header parsed from %s. Provider '%s' status reset to ACTIVE.",
                    sender, subscription.getProviderName());
        } else {
            return String.format("⚠️ [EMAIL INGRESS] Sender domain '%s' does not match any active subscription.", sender);
        }
    }

    @Transactional
    public String processBankTransaction(BankTransactionRequest transactionRequest) {
        String merchant = transactionRequest.getMerchantName();
        LocalDateTime timestamp = transactionRequest.getTransactionTimestamp() != null
                ? transactionRequest.getTransactionTimestamp()
                : LocalDateTime.now();

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByProviderNameIgnoreCase(merchant);

        if (optionalSubscription.isPresent()) {
            Subscription subscription = optionalSubscription.get();
            subscription.setLastInteractionTimestamp(timestamp);
            subscription.setStatus("ACTIVE");
            subscriptionRepository.save(subscription);

            auditLogRepository.save(new AuditLog(subscription, "BANK", "Charged amount: $" + transactionRequest.getAmount()));

            return String.format("✅ [BANK LEDGER INGRESS] Payment charge of $%.2f matched for %s. Interaction timestamp updated and status set to ACTIVE.",
                    transactionRequest.getAmount(), subscription.getProviderName());
        } else {
            return String.format("⚠️ [BANK LEDGER INGRESS] Unregistered recurring merchant charge detected for '%s'.", merchant);
        }
    }

    @Transactional
    public void evaluateDormantSubscription(Subscription sub) {
        if (sub == null) {
            return;
        }

        sub.setStatus("CRITICAL_LEAK");
        subscriptionRepository.save(sub);

        String logMessage = String.format("Automated Audit: Subscription '%s' inactive since %s. State flagged as CRITICAL_LEAK.",
                sub.getProviderName(), sub.getLastInteractionTimestamp());

        auditLogRepository.save(new AuditLog(sub, "SCHEDULED_BATCH", logMessage));
    }
}