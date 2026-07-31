package com.chronos.chronos_audit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    // 🎯 Injects the property variable from application.properties
    @Value("${chronos.alert.recipient.email:alerts@chronos.io}")
    private String alertRecipientEmail;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendCriticalLeakAlert(String subscriptionId, String providerName, double amount) {
        System.out.println("📨 [ASYNC EMAIL] Spawning thread: " + Thread.currentThread().getName());
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // 🔒 Uses the placeholder variable instead of a hardcoded email
            message.setTo(alertRecipientEmail);

            message.setSubject("🚨 CRITICAL LEAK DETECTED: " + providerName);
            message.setText("Attention,\n\n" +
                    "A recurring financial drain anomaly was intercepted:\n" +
                    "• Subscription ID: " + subscriptionId + "\n" +
                    "• Provider: " + providerName + "\n" +
                    "• Exposure: $" + amount + "\n\n" +
                    "Status updated to CRITICAL_LEAK.");

            mailSender.send(message);
            System.out.println("✅ [ASYNC EMAIL] Alert dispatched successfully for ID: " + subscriptionId);
        } catch (Exception e) {
            System.err.println("❌ [ASYNC EMAIL] Delivery failed: " + e.getMessage());
        }
    }
}