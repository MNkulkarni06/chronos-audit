package com.chronos.chronos_audit.scheduler;

import com.chronos.chronos_audit.entity.Subscription;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import com.chronos.chronos_audit.service.AuditService;
import com.chronos.chronos_audit.service.EmailNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutomatedAuditScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final AuditService auditService;
    private final EmailNotificationService emailNotificationService;

    public AutomatedAuditScheduler(SubscriptionRepository subscriptionRepository,
                                   AuditService auditService,
                                   EmailNotificationService emailNotificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.auditService = auditService;
        this.emailNotificationService = emailNotificationService;
    }

    // Runs once a day at midnight (or change to fixedRate = 30000 for testing)
    @Scheduled(fixedRate = 30000)
    public void runDormancyAuditBatch() {
        System.out.println("⏰ [BATCH ENGINE] Scanning database for dormant subscriptions...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // ✅ Only fetches dormant records that haven't been processed yet
        List<Subscription> dormantSubs =
                subscriptionRepository.findByLastInteractionTimestampBeforeAndStatusNot(thirtyDaysAgo, "CRITICAL_LEAK");

        if (dormantSubs.isEmpty()) {
            System.out.println("✅ [BATCH ENGINE] Scan complete. Zero new dormant subscription leaks detected.");
            return;
        }

        System.out.println("⚠️ [BATCH ENGINE] Found " + dormantSubs.size() + " new dormant record(s). Processing...");

        for (Subscription sub : dormantSubs) {
            // 1. Mark as CRITICAL_LEAK and insert audit log record
            auditService.evaluateDormantSubscription(sub);

            // 2. Trigger the Async Email Alert
            emailNotificationService.sendCriticalLeakAlert(
                    String.valueOf(sub.getId()),
                    sub.getProviderName(),
                    sub.getMonthlyAmount().doubleValue()
            );

            // 3. Pause briefly to stay under Mailtrap's 1 email/sec rate limit
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}