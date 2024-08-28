package com.example.account.repository;

import com.example.account.entity.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, Long> {
    PendingTransaction findByTransactionId(Long transactionId);
}
