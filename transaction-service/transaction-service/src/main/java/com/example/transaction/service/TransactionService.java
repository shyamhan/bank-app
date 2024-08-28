package com.example.transaction.service;

import com.example.transaction.entity.Transaction;
import com.example.transaction.exception.InvalidAmountException;
import com.example.transaction.exception.TransactionException;
import com.example.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    public TransactionService(TransactionRepository transactionRepository,AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient=accountServiceClient;
    }

    public Long generateTransactionId() {
        // Using a random UUID and converting it to a string for transaction ID
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    @Transactional
    public Transaction transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount, String token) {

        Long withdrawId = generateTransactionId();
        Long depositId = generateTransactionId();

        try {
                // Phase 1: Prepare
                boolean withdrawPrepared = accountServiceClient.prepareWithdraw(fromAccountId, amount, withdrawId, token);
                boolean depositPrepared = accountServiceClient.prepareDeposit(toAccountId, amount, depositId, token);

                if (withdrawPrepared && depositPrepared) {
                    // Phase 2: Commit
                    accountServiceClient.commitWithdraw(withdrawId, token);
                    accountServiceClient.commitDeposit(depositId, token);

                    // Record the transaction
                    Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, LocalDateTime.now());
                    return transactionRepository.save(transaction);
                } else {
                    // Rollback both actions
                    accountServiceClient.rollbackWithdraw(withdrawId, token);
                    accountServiceClient.rollbackDeposit(depositId, token);
                    throw new InvalidAmountException("Transaction failed during the prepare phase");
                }
            } catch (Exception ex) {
                // Handle any other exceptions and rollback
                accountServiceClient.rollbackWithdraw(withdrawId, token);
                accountServiceClient.rollbackDeposit(depositId, token);
                throw new TransactionException("Transaction failed due to "+ ex);
            }

    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }
}
