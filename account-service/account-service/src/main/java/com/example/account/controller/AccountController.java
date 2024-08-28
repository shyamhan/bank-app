package com.example.account.controller;

import com.example.account.entity.Account;
import com.example.account.service.AccountService;
import com.example.account.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final JwtUtil jwtUtil;

    private final AccountService accountService;

    public AccountController(JwtUtil jwtUtil, AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public Account createAccount(@Valid @RequestBody Account account) {
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

    @GetMapping("/api/v1/account/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        boolean isValid = jwtUtil.validateToken(jwtToken, username);
        return ResponseEntity.ok(isValid);
    }
}

