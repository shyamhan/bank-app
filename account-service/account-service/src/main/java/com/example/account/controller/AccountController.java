package com.example.account.controller;

import com.example.account.entity.Account;
import com.example.account.service.AccountService;
import com.example.account.util.JwtUtil;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private static final Log logger = LogFactory.getLog(AccountController.class);

    private final JwtUtil jwtUtil;

    private final AccountService accountService;

    public AccountController(JwtUtil jwtUtil, AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public Account createAccount(@Valid @RequestBody Account account) {
        logger.info("create account called");
        return accountService.createAccount(account);
    }

    @PostMapping("/deposit/{id}")
    public Account deposit(@PathVariable Long id, @RequestBody BigDecimal amount) {
        return accountService.deposit(id, amount);
    }

    @PostMapping("/withdraw/{id}")
    public Account withdraw(@PathVariable Long id, @RequestBody BigDecimal amount) {
        return accountService.withdraw(id, amount);
    }

    @GetMapping("/balance/{id}")
    public BigDecimal checkBalance(@PathVariable Long id) {
        return accountService.checkBalance(id);
    }

    @GetMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken() {
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PostMapping("/prepare/withdraw/{id}/{transactionID}")
    public ResponseEntity<String> prepareWithdraw(@PathVariable Long id, @PathVariable Long transactionID, @RequestBody BigDecimal amount) {
        boolean canCommit = accountService.prepareWithdraw(transactionID, id, amount);
        return canCommit ? ResponseEntity.ok("PREPARED") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ABORT");
    }

    @PostMapping("/commit/withdraw/{transactionId}")
    public ResponseEntity<Void> commitWithdraw(@PathVariable Long transactionId) {
        accountService.commitTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback/withdraw/{transactionId}")
    public ResponseEntity<Void> rollbackWithdraw(@PathVariable Long transactionId) {
        accountService.rollbackTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/prepare/deposit/{id}/{transactionID}")
    public ResponseEntity<String> prepareDeposit(@PathVariable Long id, @PathVariable Long transactionID, @RequestBody BigDecimal amount) {
        boolean canCommit = accountService.prepareDeposit(transactionID, id, amount);
        return canCommit ? ResponseEntity.ok("PREPARED") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ABORT");
    }

    @PostMapping("/commit/deposit/{transactionId}")
    public ResponseEntity<Void> commitDeposit(@PathVariable Long transactionId) {
        accountService.commitTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rollback/deposit/{transactionId}")
    public ResponseEntity<Void> rollbackDeposit(@PathVariable Long transactionId) {
        accountService.rollbackTransaction(transactionId);
        return ResponseEntity.ok().build();
    }
}

