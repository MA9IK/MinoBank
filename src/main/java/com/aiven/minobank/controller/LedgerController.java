package com.aiven.minobank.controller;

import com.aiven.minobank.records.Transaction;
import com.aiven.minobank.service.LedgerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping()
    public Transaction addTransaction(@RequestBody Transaction transaction) {
        return ledgerService.addTransaction(transaction);

    }

    @GetMapping("/balance")
    public String showBalance() {
        Double res = ledgerService.calculateTotalBalance();

        return "Your total balance is: " + res + ". You are broke man.";
    }
}
