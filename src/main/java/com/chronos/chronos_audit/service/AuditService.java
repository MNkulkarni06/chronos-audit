package com.chronos.chronos_audit.service;

import com.chronos.chronos_audit.dto.SubscriptionDTO;
import com.chronos.chronos_audit.model.Subscription;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuditService {

    private final SubscriptionRepository subscriptionRepository;

    // Direct Constructor Injection for repository persistence
    public AuditService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Map<String, Object> calculateLeakRisk(String id, SubscriptionDTO telemetry) {
        // 1. Fetch live subscription state from database
        Optional<Subscription> optionalSubscription = subscriptionRepository.findById(id);

        if (optionalSubscription.isEmpty()) {
            throw new RuntimeException("Subscription record not found for database ID: " + id);
        }

        Subscription subscription = optionalSubscription.get();
        Map<String, Object> assessment = new LinkedHashMap<>();
        int leakScore = 0;

        // 2. Evaluate weighted risk business logic matrices
        if (!telemetry.isHasNetworkActivity()) leakScore += 40;
        if (!telemetry.isHasEmailActivity()) leakScore += 30;
        if (telemetry.isHasRecurringCharge()) leakScore += 30;

        String calculatedStatus;
        if (leakScore >= 70) {
            calculatedStatus = "CRITICAL_LEAK";
        } else if (leakScore >= 40) {
            calculatedStatus = "POTENTIAL_LEAK";
        } else {
            calculatedStatus = "HEALTHY_ACTIVE";
        }

        // 3. Mutate entity state and refresh timestamps based on traffic signals
        subscription.setStatus(calculatedStatus);
        if (telemetry.isHasNetworkActivity() || telemetry.isHasEmailActivity()) {
            subscription.setLastInteractionTimestamp(LocalDateTime.now());
        }

        // 4. Persistence Step: Push mutated states back down to MySQL records
        subscriptionRepository.save(subscription);

        // 5. Construct payload return structure
        assessment.put("subscriptionId", subscription.getId());
        assessment.put("providerName", subscription.getProviderName());
        assessment.put("leakScore", leakScore + "%");
        assessment.put("status", subscription.getStatus());
        assessment.put("breakdown", Map.of(
                "networkDormant", !telemetry.isHasNetworkActivity(),
                "emailDormant", !telemetry.isHasEmailActivity(),
                "billingActive", telemetry.isHasRecurringCharge()
        ));

        return assessment;
    }
}