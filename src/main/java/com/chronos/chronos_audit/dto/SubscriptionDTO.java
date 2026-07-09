package com.chronos.chronos_audit.dto;

public class SubscriptionDTO {
    // Simulated input flags from our data adapters (Email, Bank, DNS logs)
    private boolean hasNetworkActivity;
    private boolean hasEmailActivity;
    private boolean hasRecurringCharge;

    // Getters and Setters
    public boolean isHasNetworkActivity() { return hasNetworkActivity; }
    public void setHasNetworkActivity(boolean hasNetworkActivity) { this.hasNetworkActivity = hasNetworkActivity; }
    public boolean isHasEmailActivity() { return hasEmailActivity; }
    public void setHasEmailActivity(boolean hasEmailActivity) { this.hasEmailActivity = hasEmailActivity; }
    public boolean isHasRecurringCharge() { return hasRecurringCharge; }
    public void setHasRecurringCharge(boolean hasRecurringCharge) { this.hasRecurringCharge = hasRecurringCharge; }
}