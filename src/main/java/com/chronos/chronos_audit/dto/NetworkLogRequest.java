package com.chronos.chronos_audit.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class NetworkLogRequest {

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Domain name is required")
    private String domain;

    private LocalDateTime timestamp;

    public NetworkLogRequest() {}

    public NetworkLogRequest(String providerName, String domain, LocalDateTime timestamp) {
        this.providerName = providerName;
        this.domain = domain;
        this.timestamp = timestamp;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}