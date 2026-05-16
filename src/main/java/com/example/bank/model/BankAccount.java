package com.example.bank.model;

import java.time.LocalDateTime;

public class BankAccount {

    private Long id;
    private String accountHolder;
    private String accountNumber;
    private Double balance;
    private LocalDateTime createdAt;

    public BankAccount() {}

    public BankAccount(Long id, String accountHolder, String accountNumber, Double balance) {
        this.id = id;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
