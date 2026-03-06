package com.resurrection_finance.repository;

import com.resurrection_finance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByOriginAccountUserExternalIdOrDestinationAccountUserExternalId(UUID originId, UUID destId, Pageable pageable);

}
