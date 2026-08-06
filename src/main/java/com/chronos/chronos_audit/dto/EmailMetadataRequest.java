package com.chronos.chronos_audit.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class EmailMetadataRequest {

    @NotBlank(message = "Sender email address is required")
    private String senderEmail;

    @NotBlank(message = "Subject line is required")
    private String subject;

    private LocalDateTime receivedTimestamp;

    public EmailMetadataRequest() {}

    public EmailMetadataRequest(String senderEmail, String subject, LocalDateTime receivedTimestamp) {
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.receivedTimestamp = receivedTimestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public void setReceivedTimestamp(LocalDateTime receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }
}