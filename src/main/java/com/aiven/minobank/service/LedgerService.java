package com.aiven.minobank.service;


import com.aiven.minobank.enums.TransactionType;
import com.aiven.minobank.records.Account;
import com.aiven.minobank.records.Transaction;
import com.aiven.minobank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LedgerService {
    private final TransactionRepository transactionRepository;

    public LedgerService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction addTransaction(Transaction t) {
        return transactionRepository.save(t);
    }

    public Double calculateTotalBalance() {

        return transactionRepository.calculateTotalBalance();
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionRepository.getAllByType(type);
    }

    public Transaction getLargestTransaction() {
        return transactionRepository.findFirstByOrderByAmountDesc();
    }

    public void processNotification(Object event) {
        switch (event) {
            case Transaction t -> System.out.println("Processed transaction " + t.getId() + " for " + t.getAmount());
            case Account account -> System.out.println("Account " + account.accountId() + " belongs to " + account.ownerName());
            case String str -> System.out.println("System alert: " + str);
            default -> throw new IllegalStateException("Unexpected value: " + event);
        }
    }
}
