package com.example.transaction.service;

import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final Tracer tracer;

    public TransactionService(TransactionRepository transactionRepository, Tracer tracer) {
        this.transactionRepository = transactionRepository;
        this.tracer = tracer;
    }

    @Transactional
    public Transaction transferMoney(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        // Start a new trace span for the transfer
        var span = tracer.nextSpan().name("transferMoney").start();
        try (var ws = tracer.withSpan(span)) {
            // Create a new transaction record
            Transaction transaction = new Transaction();
            transaction.setSourceAccountId(sourceAccountId);
            transaction.setTargetAccountId(targetAccountId);
            transaction.setAmount(amount);
            transaction.setTimestamp(LocalDateTime.now());

            return transactionRepository.save(transaction);
        } finally {
            span.end();
        }
    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findBySourceAccountIdOrTargetAccountId(accountId, accountId);
    }
}
