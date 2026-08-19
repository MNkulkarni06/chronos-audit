package com.chronos.chronos_audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${chronos.alert.recipient.email:alerts@chronos.io}")
    private String alertRecipientEmail;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendCriticalLeakAlert(String userEmail, Long subscriptionId, String providerName, BigDecimal amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("notifications@chronos-audit.io");
            message.setTo((userEmail != null && !userEmail.isBlank()) ? userEmail : alertRecipientEmail);
            message.setSubject("🚨 CRITICAL LEAK DETECTED: " + providerName);
            message.setText("Attention,\n\n" +
                    "A recurring financial drain anomaly was intercepted:\n" +
                    "• Subscription ID: " + subscriptionId + "\n" +
                    "• Provider: " + providerName + "\n" +
                    "• Exposure: $" + amount + "\n\n" +
                    "Status updated to CRITICAL_LEAK.");

            mailSender.send(message);
            log.info("✅ [ASYNC EMAIL] Alert dispatched successfully for provider {}", providerName);
        } catch (Exception e) {
            log.error("❌ [ASYNC EMAIL] Failed to dispatch alert for {}: {}", providerName, e.getMessage(), e);
        }
    }

    @Async
    public void sendUpcomingRenewalAlert(String userEmail, String providerName, BigDecimal amount, LocalDateTime renewalDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("notifications@chronos-audit.io");
            message.setTo((userEmail != null && !userEmail.isBlank()) ? userEmail : alertRecipientEmail);
            message.setSubject("🔔 Upcoming Subscription Renewal: " + providerName);
            message.setText("Hello,\n\n" +
                    "This is an automated reminder that your subscription is scheduled for renewal:\n" +
                    "• Provider: " + providerName + "\n" +
                    "• Amount: $" + amount + "\n" +
                    "• Renewal Date: " + renewalDate + "\n\n" +
                    "If you no longer use this service, please cancel before the billing date.");

            mailSender.send(message);
            log.info("✅ [ASYNC EMAIL] Renewal reminder sent successfully for {}", providerName);
        } catch (Exception e) {
            log.error("❌ [ASYNC EMAIL] Failed to send renewal reminder for {}: {}", providerName, e.getMessage(), e);
        }
    }
}