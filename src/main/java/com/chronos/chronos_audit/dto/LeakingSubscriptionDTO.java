package com.chronos.chronos_audit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeakingSubscriptionDTO {
    private String userEmail;
    private String userName;
    private String providerName;
    private BigDecimal monthlyAmount;
    private LocalDateTime lastInteractionTimestamp;

    public LeakingSubscriptionDTO(String userEmail, String userName, String providerName, BigDecimal monthlyAmount, LocalDateTime lastInteractionTimestamp) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.providerName = providerName;
        this.monthlyAmount = monthlyAmount;
        this.lastInteractionTimestamp = lastInteractionTimestamp;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public BigDecimal getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(BigDecimal monthlyAmount) { this.monthlyAmount = monthlyAmount; }
    public LocalDateTime getLastInteractionTimestamp() { return lastInteractionTimestamp; }
    public void setLastInteractionTimestamp(LocalDateTime lastInteractionTimestamp) { this.lastInteractionTimestamp = lastInteractionTimestamp; }
}