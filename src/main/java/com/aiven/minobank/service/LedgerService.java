package com.aiven.minobank.service;


import com.aiven.minobank.enums.TransactionType;
import com.aiven.minobank.records.Account;
import com.aiven.minobank.records.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LedgerService {
    private final List<Transaction> transactions = new ArrayList<>();

    public Transaction addTransaction(Transaction t) {
        transactions.add(t);
        return t;
    }

    public double calculateTotalBalance() {
        return transactions.stream().mapToDouble(t ->
            switch (t.type()) {
                case DEPOSIT, TRANSFER -> t.amount();
                case WITHDRAWAL -> -t.amount();
            }).sum();
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactions.stream().filter(o -> o.type() == type).toList();
    }

    public Transaction getLargestTransaction() {
        return transactions.stream().max(Comparator.comparingDouble(Transaction::amount)).orElseThrow();
    }

    public void processNotification(Object event) {
        switch (event) {
            case Transaction t -> System.out.println("Processed transaction " + t.id() + " for " + t.amount());
            case Account account -> System.out.println("Account " + account.accountId() + " belongs to " + account.ownerName());
            case String str -> System.out.println("System alert: " + str);
            default -> throw new IllegalStateException("Unexpected value: " + event);
        }
    }
}
