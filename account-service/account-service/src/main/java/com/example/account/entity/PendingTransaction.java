package com.example.account.entity;

import com.example.account.util.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingTransaction {

    @Id
    private Long transactionId;
    private Long accountId;
    private BigDecimal amount;
    private TransactionType type;
    private String status; // 'PENDING', 'COMMITTED', 'ABORTED'

}
