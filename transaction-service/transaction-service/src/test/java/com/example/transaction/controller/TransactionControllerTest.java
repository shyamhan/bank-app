package com.example.transaction.controller;

import com.example.transaction.entity.Transaction;
import com.example.transaction.request.TransferRequest;
import com.example.transaction.service.TokenValidationService;
import com.example.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionControllerTests {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TokenValidationService tokenValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void transferMoney_shouldReturnSuccess_whenTokenIsValid() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100));
        String token = "valid-token";

        when(tokenValidationService.validateToken(token)).thenReturn(true);
        when(transactionService.transferMoney(eq(1L), eq(2L), eq(BigDecimal.valueOf(100)), eq(token)))
                .thenReturn(new Transaction()); // Assuming transferMoney returns a Transaction object

        ResponseEntity<String> response = transactionController.transferMoney(transferRequest, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("successfully transferred", response.getBody());
        verify(tokenValidationService).validateToken(token);
        verify(transactionService).transferMoney(eq(1L), eq(2L), eq(BigDecimal.valueOf(100)), eq(token));
    }

    @Test
    void transferMoney_shouldReturnUnauthorized_whenTokenIsInvalid() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100));
        String token = "invalid-token";

        when(tokenValidationService.validateToken(token)).thenReturn(false);

        ResponseEntity<String> response = transactionController.transferMoney(transferRequest, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid Token", response.getBody());
        verify(tokenValidationService).validateToken(token);
        verify(transactionService, never()).transferMoney(anyLong(), anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    void getTransactionHistory_shouldReturnTransactionHistory_whenTokenIsValid() {
        Long accountId = 1L;
        String token = "valid-token";
        List<Transaction> transactions = Arrays.asList(new Transaction(), new Transaction());

        when(tokenValidationService.validateToken(token)).thenReturn(true);
        when(transactionService.getTransactionHistory(accountId)).thenReturn(transactions);

        ResponseEntity<?> response = transactionController.getTransactionHistory(token, accountId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
        verify(tokenValidationService).validateToken(token);
        verify(transactionService).getTransactionHistory(accountId);
    }

    @Test
    void getTransactionHistory_shouldReturnUnauthorized_whenTokenIsInvalid() {
        Long accountId = 1L;
        String token = "invalid-token";

        when(tokenValidationService.validateToken(token)).thenReturn(false);

        ResponseEntity<?> response = transactionController.getTransactionHistory(token, accountId);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid Token", response.getBody());
        verify(tokenValidationService).validateToken(token);
        verify(transactionService, never()).getTransactionHistory(anyLong());
    }
}
