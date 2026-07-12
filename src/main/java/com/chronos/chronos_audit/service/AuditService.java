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
        // Update this specific block inside your AuditService.java file
        Optional<Subscription> optionalSubscription = subscriptionRepository.findById(id);

        if (optionalSubscription.isEmpty()) {
            // 🔍 Throwing our explicit, decoupled enterprise exception
            throw new com.chronos.chronos_audit.exception.ResourceNotFoundException(
                    "Subscription record not found for database ID: " + id
            );
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

    // Add this method to evaluate existing database records dynamically
    public void evaluateDormantSubscription(Subscription subscription) {
        int leakScore = 0;

        // Core Business Logic: If a subscription hasn't been updated in 30 days,
        // it implies telemetry channels (Email/Network) have gone cold.
        // Since it's still in our DB, the recurring financial charge is active.
        leakScore += 40; // Weight: No matching DNS logs logged recently
        leakScore += 30; // Weight: No active email interactions parsed
        leakScore += 30; // Weight: Passive billing loop active

        subscription.setStatus("CRITICAL_LEAK");

        // Save the updated status back to MySQL
        subscriptionRepository.save(subscription);

        System.out.println("   [BATCH ASSESS] ID: " + subscription.getId()
                + " | Provider: " + subscription.getProviderName()
                + " | Status Updated to: CRITICAL_LEAK (" + leakScore + "%)");
    }
}