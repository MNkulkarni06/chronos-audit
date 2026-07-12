package com.chronos.chronos_audit.scheduler;

import com.chronos.chronos_audit.model.Subscription;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import com.chronos.chronos_audit.service.AuditService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutomatedAuditScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final AuditService auditService;

    public AutomatedAuditScheduler(SubscriptionRepository subscriptionRepository, AuditService auditService) {
        this.subscriptionRepository = subscriptionRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedRate = 30000)
    public void runBatchLeakAuditing() {
        System.out.println("⏰ [BATCH ENGINE] Scanning database for subscriptions with no activity for 30+ days...");

        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);

        // 🔍 FIX 1: Change the Generic type from List<String> to List<Subscription>
        List<Subscription> dormantSubscriptions = subscriptionRepository.findByLastInteractionTimestampBefore(thresholdDate);

        if (dormantSubscriptions.isEmpty()) {
            System.out.println("✅ [BATCH ENGINE] Scan complete. Zero dormant subscription leaks detected.");
            return;
        }

        System.out.println("⚠️ [BATCH ENGINE] Found " + dormantSubscriptions.size() + " dormant records. Processing updates...");

        // 🔄 FIX 2: Ensure the loop variable matches the structural Entity type
        for (Subscription sub : dormantSubscriptions) {
            try {
                auditService.evaluateDormantSubscription(sub);
            } catch (Exception e) {
                System.err.println("❌ Error auditing subscription ID " + sub.getId() + ": " + e.getMessage());
            }
        }
        System.out.println("----------------------------------------------------------------------");
    }
}