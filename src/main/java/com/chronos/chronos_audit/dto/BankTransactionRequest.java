package com.chronos.chronos_audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankTransactionRequest {

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotNull(message = "Transaction amount is required")
    private BigDecimal amount;

    private LocalDateTime transactionTimestamp;

    public BankTransactionRequest() {}

    public BankTransactionRequest(String merchantName, BigDecimal amount, LocalDateTime transactionTimestamp) {
        this.merchantName = merchantName;
        this.amount = amount;
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(LocalDateTime transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }
}