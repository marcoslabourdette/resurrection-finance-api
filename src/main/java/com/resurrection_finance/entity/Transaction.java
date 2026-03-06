package com.resurrection_finance.entity;

import com.resurrection_finance.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @CreationTimestamp
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "origin_account_id")
    private Account originAccount;
    @ManyToOne
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;
    @Column(unique = true, nullable = false, updatable = false)
    private UUID transactionId = UUID.randomUUID();
}
