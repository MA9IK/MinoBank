package com.aiven.minobank.records;

import com.aiven.minobank.enums.TransactionType;

import java.time.LocalDateTime;

public record Transaction(
        String id,
        double amount,
        TransactionType type,
        LocalDateTime timestamp
) {}
