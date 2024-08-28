package com.example.account.service;

import com.example.account.entity.Account;
import com.example.account.entity.PendingTransaction;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.exception.DuplicateEmailException;
import com.example.account.exception.InsufficientDataException;
import com.example.account.exception.InvalidAmountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.PendingTransactionRepository;
import com.example.account.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PendingTransactionRepository pendingTransactionRepository;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldSaveAccount_whenValidAccount() {
        Account account = new Account();
        account.setEmail("test@example.com");
        account.setName("Test User");
        account.setPhoneNumber("12345678890");
        account.setPassword("test");

        when(accountRepository.findByEmail(account.getEmail())).thenReturn(Optional.empty());
        when(accountRepository.save(account)).thenReturn(account);

        Account createdAccount = accountService.createAccount(account);

        assertNotNull(createdAccount);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void createAccount_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        Account account = new Account();
        account.setEmail("test@example.com");

        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(account));

        assertThrows(DuplicateEmailException.class, () -> accountService.createAccount(account));
        verify(accountRepository, never()).save(account);
    }

    @Test
    void deposit_shouldIncreaseBalance_whenAmountIsValid() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account updatedAccount = accountService.deposit(accountId, amount);

        assertEquals(BigDecimal.valueOf(300), updatedAccount.getBalance());
        verify(accountRepository, times(1)).save(account);

    }

    @Test
    void withdraw_shouldDecreaseBalance_whenSufficientFunds() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account updatedAccount = accountService.withdraw(accountId, amount);

        assertEquals(BigDecimal.valueOf(100), updatedAccount.getBalance());
        verify(accountRepository, times(1)).save(account);

    }

    @Test
    void withdraw_shouldThrowInvalidAmountException_whenInsufficientFunds() {
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(300);
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(InvalidAmountException.class, () -> accountService.withdraw(accountId, amount));
        verify(accountRepository, never()).save(account);
        verify(notificationServiceClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void commitTransaction_shouldCommitPendingTransaction_whenTransactionIsPending() {
        Long transactionId = 1L;
        PendingTransaction pendingTransaction = new PendingTransaction();
        pendingTransaction.setTransactionId(transactionId);
        pendingTransaction.setAccountId(1L);
        pendingTransaction.setAmount(BigDecimal.valueOf(100));
        pendingTransaction.setType(TransactionType.DEPOSIT);
        pendingTransaction.setStatus("PENDING");

        Account account = new Account();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(200));

        when(pendingTransactionRepository.findByTransactionId(transactionId)).thenReturn(pendingTransaction);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.commitTransaction(transactionId);

        verify(accountRepository, times(1)).save(account);
        verify(pendingTransactionRepository, times(1)).save(pendingTransaction);

    }
}

