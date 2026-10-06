package com.aiven.minobank.controller;

import com.aiven.minobank.service.LedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

//    @PostMapping()
//    public void

    @GetMapping("/balance")
    public String showBalance() {
        double res = ledgerService.calculateTotalBalance();

        return "Your total balance is: " + res + ". You are broke man.";
    }
}
