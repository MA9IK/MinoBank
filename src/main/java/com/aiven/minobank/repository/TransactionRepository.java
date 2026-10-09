package com.aiven.minobank.repository;

import com.aiven.minobank.enums.TransactionType;
import com.aiven.minobank.records.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> getAllByType(TransactionType type);
    Collection<Object> getAllByTypeNot(TransactionType type);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = com.aiven.minobank.enums.TransactionType.WITHDRAWAL THEN -t.amount ELSE t.amount END), 0.0) FROM Transaction t")
    public Double calculateTotalBalance();

    Transaction findFirstByOrderByAmountDesc();
}
