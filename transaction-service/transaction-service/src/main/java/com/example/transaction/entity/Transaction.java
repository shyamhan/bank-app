package com.example.transaction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public Transaction(Long fromAccountId, Long toAccountId, BigDecimal amount, LocalDateTime now) {
        this.sourceAccountId=fromAccountId;
        this.targetAccountId=toAccountId;
        this.amount=amount;
        this.timestamp=now;
    }
}
