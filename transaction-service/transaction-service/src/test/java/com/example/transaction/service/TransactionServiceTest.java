package com.example.transaction.service;

import com.example.transaction.entity.Transaction;
import com.example.transaction.exception.InvalidAmountException;
import com.example.transaction.exception.TransactionException;
import com.example.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class TransactionServiceTests {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void transferMoney_shouldSucceed_whenPrepareAndCommitAreSuccessful() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        String token = "valid-token";

        Long withdrawId = 1L;
        Long depositId = 2L;

        when(accountServiceClient.prepareWithdraw(eq(fromAccountId), eq(amount), anyLong(), eq(token)))
                .thenReturn(true);
        when(accountServiceClient.prepareDeposit(eq(toAccountId), eq(amount), anyLong(), eq(token)))
                .thenReturn(true);

        doNothing().when(accountServiceClient).commitDeposit(any(), any());
        doNothing().when(accountServiceClient).commitWithdraw(any(), any());

        Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, LocalDateTime.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.transferMoney(fromAccountId, toAccountId, amount, token);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getSourceAccountId());
        assertEquals(toAccountId, result.getTargetAccountId());
        assertEquals(amount, result.getAmount());

        verify(accountServiceClient).prepareWithdraw(eq(fromAccountId), eq(amount), anyLong(), eq(token));
        verify(accountServiceClient).prepareDeposit(eq(toAccountId), eq(amount), anyLong(), eq(token));
        verify(accountServiceClient).commitWithdraw(anyLong(), eq(token));
        verify(accountServiceClient).commitDeposit(anyLong(), eq(token));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferMoney_shouldThrowTransactionException_whenExceptionOccurs() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        String token = "valid-token";

        Long withdrawId = 1L;
        Long depositId = 2L;

        when(accountServiceClient.prepareWithdraw(eq(fromAccountId), eq(amount), anyLong(), eq(token)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(TransactionException.class, () ->
                transactionService.transferMoney(fromAccountId, toAccountId, amount, token));

        verify(accountServiceClient).prepareWithdraw(eq(fromAccountId), eq(amount), anyLong(), eq(token));
        verify(accountServiceClient).rollbackWithdraw(anyLong(), eq(token));
        verify(accountServiceClient).rollbackDeposit(anyLong(), eq(token));
    }

    @Test
    void getTransactionHistory_shouldReturnListOfTransactions() {
        Long accountId = 1L;
        Transaction transaction1 = new Transaction(accountId, 2L, BigDecimal.valueOf(100), LocalDateTime.now());
        Transaction transaction2 = new Transaction(3L, accountId, BigDecimal.valueOf(50), LocalDateTime.now());

        when(transactionRepository.findBySourceAccountIdOrTargetAccountId(accountId, accountId))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        List<Transaction> transactions = transactionService.getTransactionHistory(accountId);

        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        assertTrue(transactions.contains(transaction1));
        assertTrue(transactions.contains(transaction2));
    }
}

