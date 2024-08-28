package com.example.transaction.controller;

import com.example.transaction.request.TransferRequest;
import com.example.transaction.service.TokenValidationService;
import com.example.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TokenValidationService tokenValidationService;


    public TransactionController(TransactionService transactionService, TokenValidationService tokenValidationService) {
        this.transactionService = transactionService;
        this.tokenValidationService=tokenValidationService;
    }

    @Transactional
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody TransferRequest transferRequest, @RequestHeader("Authorization") String token) {
        if (tokenValidationService.validateToken(token)) {
           transactionService.transferMoney(transferRequest.getFromAccountId(), transferRequest.getToAccountId(), transferRequest.getAmount(), token);
            return ResponseEntity.status(HttpStatus.OK).body("successfully transferred");
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

    }


    @GetMapping("/history/{accountId}")
    public ResponseEntity<?> getTransactionHistory(@RequestHeader("Authorization") String token, @PathVariable Long accountId) {

        // Validate the token using the tokenValidationService
        if (tokenValidationService.validateToken(token)) {
            // Proceed with fetching transaction history
            return ResponseEntity.ok(transactionService.getTransactionHistory(accountId));
        } else {
            // Return an unauthorized status if the token is invalid
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
    }

}
