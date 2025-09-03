package models;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {
    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }
    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal prevBalance;
    private BigDecimal newBalance;
    private String failureReason;  // ADDED: Missing field
    private Instant creationTime;
    private Instant processedTime;

    public Transaction() {}

    public Transaction(String transactionId, String accountId, BigDecimal amount,
                       TransactionType type, TransactionStatus status, BigDecimal prevBalance,
                       BigDecimal newBalance, String failureReason, Instant creationTime,
                       Instant processedTime) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.prevBalance = prevBalance;
        this.newBalance = newBalance;
        this.failureReason = failureReason;  // ADDED: Missing field
        this.creationTime = creationTime;
        this.processedTime = processedTime;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public BigDecimal getPrevBalance() {
        return prevBalance;
    }

    public void setPrevBalance(BigDecimal prevBalance) {
        this.prevBalance = prevBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    // ADDED: Missing getter and setter for failureReason
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public Instant getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Instant processedTime) {
        this.processedTime = processedTime;
    }
}