package com.chronos.chronos_audit.scheduler;

import com.chronos.chronos_audit.entity.Subscription;
import com.chronos.chronos_audit.repository.SubscriptionRepository;
import com.chronos.chronos_audit.service.AuditService;
import com.chronos.chronos_audit.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutomatedAuditScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutomatedAuditScheduler.class);

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

    @Transactional(readOnly = true)
    @Scheduled(cron = "${audit.scheduler.cron:0 0 0 * * ?}")
    public void runDormancyAuditBatch() {
        log.info("⏰ [BATCH ENGINE] Scanning database for dormant subscriptions...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Subscription> dormantSubs =
                subscriptionRepository.findByLastInteractionTimestampBeforeAndStatusNot(thirtyDaysAgo, "CRITICAL_LEAK");

        if (dormantSubs.isEmpty()) {
            log.info("✅ [BATCH ENGINE] Scan complete. Zero new dormant subscription leaks detected.");
            return;
        }

        log.warn("⚠️ [BATCH ENGINE] Found {} new dormant record(s). Processing batch...", dormantSubs.size());

        for (Subscription sub : dormantSubs) {
            try {
                auditService.evaluateDormantSubscription(sub);

                String userEmail = (sub.getUser() != null) ? sub.getUser().getEmail() : null;

                emailNotificationService.sendCriticalLeakAlert(
                        userEmail,
                        sub.getId(),
                        sub.getProviderName(),
                        sub.getMonthlyAmount()
                );
            } catch (Exception ex) {
                log.error("❌ Failed to process subscription ID {}: {}. Continuing remaining batch.", sub.getId(), ex.getMessage(), ex);
            }
        }

        log.info("🏁 [BATCH ENGINE] Completed batch sweep.");
    }

    @Transactional(readOnly = true)
    @Scheduled(cron = "${audit.scheduler.cron:0 0 0 * * ?}")
    public void executeRenewalAlertSweep() {
        log.info("⏰ [RENEWAL ENGINE] Checking for subscriptions renewing in the next 48 hours...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(2);

        List<Subscription> upcoming = subscriptionRepository.findUpcomingRenewals(now, windowEnd);

        if (upcoming.isEmpty()) {
            log.info("✅ [RENEWAL ENGINE] No upcoming renewals detected.");
            return;
        }

        for (Subscription sub : upcoming) {
            try {
                String userEmail = (sub.getUser() != null) ? sub.getUser().getEmail() : null;

                emailNotificationService.sendUpcomingRenewalAlert(
                        userEmail,
                        sub.getProviderName(),
                        sub.getMonthlyAmount(),
                        sub.getNextBillingDate()
                );
            } catch (Exception e) {
                log.error("❌ [RENEWAL ENGINE] Error processing renewal reminder for ID {}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}