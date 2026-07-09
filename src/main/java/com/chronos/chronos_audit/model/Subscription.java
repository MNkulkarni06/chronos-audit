package com.chronos.chronos_audit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    private String id; // Matches the String ID parameter passed in routes
    private String providerName;
    private double monthlyAmount;
    private LocalDateTime lastInteractionTimestamp;
    private String status; // HEALTHY_ACTIVE, POTENTIAL_LEAK, CRITICAL_LEAK

    // Default No-Args Constructor required by JPA Hibernate
    public Subscription() {}

    // Overloaded Parameterized Constructor
    public Subscription(String id, String providerName, double monthlyAmount, LocalDateTime lastInteractionTimestamp, String status) {
        this.id = id;
        this.providerName = providerName;
        this.monthlyAmount = monthlyAmount;
        this.lastInteractionTimestamp = lastInteractionTimestamp;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public double getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(double monthlyAmount) { this.monthlyAmount = monthlyAmount; }
    public LocalDateTime getLastInteractionTimestamp() { return lastInteractionTimestamp; }
    public void setLastInteractionTimestamp(LocalDateTime lastInteractionTimestamp) { this.lastInteractionTimestamp = lastInteractionTimestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}