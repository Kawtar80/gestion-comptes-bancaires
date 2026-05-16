package com.example.bank.service;

import com.example.bank.model.BankAccount;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BankAccountService {

    private final Map<Long, BankAccount> accounts = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        save(new BankAccount(null, "Alice Martin", "FR76 1234 5678 9012 3456 7890 123", 1500.00));
        save(new BankAccount(null, "Bob Dupont", "FR76 9876 5432 1098 7654 3210 987", 3200.50));
    }

    public List<BankAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    public BankAccount findById(Long id) {
        BankAccount account = accounts.get(id);
        if (account == null) {
            throw new IllegalArgumentException("Compte introuvable : " + id);
        }
        return account;
    }

    public BankAccount save(BankAccount account) {
        if (account.getId() == null) {
            account.setId(idGenerator.getAndIncrement());
            if (account.getCreatedAt() == null) {
                account.setCreatedAt(java.time.LocalDateTime.now());
            }
        }
        accounts.put(account.getId(), account);
        return account;
    }

    public void deleteById(Long id) {
        accounts.remove(id);
    }

    public BankAccount deposit(Long id, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être positif.");
        }
        BankAccount account = findById(id);
        account.setBalance(account.getBalance() + amount);
        return account;
    }

    public BankAccount withdraw(Long id, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant du retrait doit être positif.");
        }
        BankAccount account = findById(id);
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Solde insuffisant. Solde actuel : " + account.getBalance() + " €");
        }
        account.setBalance(account.getBalance() - amount);
        return account;
    }
}
