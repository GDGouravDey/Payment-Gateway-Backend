package models;

import java.math.BigDecimal;
import java.time.Instant;


public class User {
    public enum Currency {
        INR, USD, EUR, GBP, JPY
    }

    private String userId;
    private String username;
    private BigDecimal balance;
    private Currency currency;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(String userId, String username, BigDecimal balance, Currency currency, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
