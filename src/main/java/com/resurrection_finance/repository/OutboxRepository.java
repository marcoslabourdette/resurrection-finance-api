package com.resurrection_finance.repository;

import com.resurrection_finance.entity.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByProcessedFalseAndRetryCountLessThan(int maxRetries);
    List<OutboxEvent> findTop20ByProcessedFalseAndRetryCountLessThan(int maxRetries);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxEvent o WHERE o.processed = false AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findToProcessWithLock(@Param("maxRetries") int maxRetries, Pageable pageable);
}
