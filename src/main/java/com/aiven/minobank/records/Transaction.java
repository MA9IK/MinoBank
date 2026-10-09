package com.aiven.minobank.records;

import com.aiven.minobank.enums.TransactionType;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private double amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private LocalDateTime created_at;

    public Transaction() {}

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }


    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public TransactionType getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
