package com.example.account.controller;

import com.example.account.entity.Account;
import com.example.account.service.AccountService;
import com.example.account.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldReturnCreatedAccount_whenValidAccount() {
        Account account = new Account();
        account.setEmail("test@example.com");

        when(accountService.createAccount(account)).thenReturn(account);

        Account createdAccount = accountController.createAccount(account);

        assertNotNull(createdAccount);
        assertEquals("test@example.com", createdAccount.getEmail());
        verify(accountService, times(1)).createAccount(account);
    }

    @Test
    void deposit_shouldReturnUpdatedAccount_whenValidAmount() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(200));

        when(accountService.deposit(accountId, amount)).thenReturn(account);

        Account updatedAccount = accountController.deposit(accountId, amount);

        assertNotNull(updatedAccount);
        assertEquals(BigDecimal.valueOf(200), updatedAccount.getBalance());
        verify(accountService, times(1)).deposit(accountId, amount);
    }

    @Test
    void withdraw_shouldReturnUpdatedAccount_whenValidAmount() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(200));

        when(accountService.withdraw(accountId, amount)).thenReturn(account);

        Account updatedAccount = accountController.withdraw(accountId, amount);

        assertNotNull(updatedAccount);
        assertEquals(BigDecimal.valueOf(200), updatedAccount.getBalance());
        verify(accountService, times(1)).withdraw(accountId, amount);
    }

    @Test
    void checkBalance_shouldReturnBalance_whenValidAccountId() {
        Long accountId = 1L;
        BigDecimal balance = BigDecimal.valueOf(200);

        when(accountService.checkBalance(accountId)).thenReturn(balance);

        BigDecimal accountBalance = accountController.checkBalance(accountId);

        assertNotNull(accountBalance);
        assertEquals(balance, accountBalance);
        verify(accountService, times(1)).checkBalance(accountId);
    }

    @Test
    void validateToken_shouldReturnTrue() {
        ResponseEntity<Boolean> response = accountController.validateToken();
        assertNotNull(response);
        assertTrue(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void prepareWithdraw_shouldReturnPrepared_whenValidTransaction() {
        Long accountId = 1L;
        Long transactionId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);

        when(accountService.prepareWithdraw(transactionId, accountId, amount)).thenReturn(true);

        ResponseEntity<String> response = accountController.prepareWithdraw(accountId, transactionId, amount);

        assertEquals("PREPARED", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).prepareWithdraw(transactionId, accountId, amount);
    }

    @Test
    void commitWithdraw_shouldReturnOk_whenValidTransactionId() {
        Long transactionId = 1L;

        ResponseEntity<Void> response = accountController.commitWithdraw(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).commitTransaction(transactionId);
    }

    @Test
    void rollbackWithdraw_shouldReturnOk_whenValidTransactionId() {
        Long transactionId = 1L;

        ResponseEntity<Void> response = accountController.rollbackWithdraw(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).rollbackTransaction(transactionId);
    }

    @Test
    void prepareDeposit_shouldReturnPrepared_whenValidTransaction() {
        Long accountId = 1L;
        Long transactionId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);

        when(accountService.prepareDeposit(transactionId, accountId, amount)).thenReturn(true);

        ResponseEntity<String> response = accountController.prepareDeposit(accountId, transactionId, amount);

        assertEquals("PREPARED", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).prepareDeposit(transactionId, accountId, amount);
    }

    @Test
    void commitDeposit_shouldReturnOk_whenValidTransactionId() {
        Long transactionId = 1L;

        ResponseEntity<Void> response = accountController.commitDeposit(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).commitTransaction(transactionId);
    }

    @Test
    void rollbackDeposit_shouldReturnOk_whenValidTransactionId() {
        Long transactionId = 1L;

        ResponseEntity<Void> response = accountController.rollbackDeposit(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).rollbackTransaction(transactionId);
    }
}

