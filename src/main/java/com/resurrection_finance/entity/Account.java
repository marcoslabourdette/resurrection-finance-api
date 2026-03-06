package com.resurrection_finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DecimalMin(value = "0.0", inclusive = false, message = "Balance must be greater than 0")
    @Column(precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(unique = true, nullable = false, updatable = false)
    private String cvu;
    @OneToOne
    private User user;
}

