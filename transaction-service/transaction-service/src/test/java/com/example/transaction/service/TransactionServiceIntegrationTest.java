package com.example.transaction.service;

import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setup() {
        // Any additional setup can be done here
    }

    @Test
    public void testTransferMoney() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        String token = "valid-jwt-token";

        // Mock the AccountServiceClient calls
        when(accountServiceClient.prepareWithdraw(any(), any(), anyLong(), any())).thenReturn(true);
        when(accountServiceClient.prepareDeposit(any(), any(), anyLong(), any())).thenReturn(true);
        doNothing().when(accountServiceClient).commitWithdraw(anyLong(), any());
        doNothing().when(accountServiceClient).commitDeposit(anyLong(), any());

        // When
        Transaction transaction = transactionService.transferMoney(fromAccountId, toAccountId, amount, token);

        // Then
        assertNotNull(transaction);
        assertEquals(fromAccountId, transaction.getSourceAccountId());
        assertEquals(toAccountId, transaction.getTargetAccountId());
        assertEquals(amount, transaction.getAmount());
    }
}
