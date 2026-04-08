package com.resurrection_finance.service;

import com.resurrection_finance.entity.OutboxEvent;
import com.resurrection_finance.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j
@Service
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(OutboxEvent event) {

        event.setLastAttemptAt(LocalDateTime.now());
        try {
            log.info("🚀 Attempting to send event {} to topic {}", event.getId(), event.getTopic());
            kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
            event.setProcessed(true);
            log.info("✅ KAFKA CONFIRMED: Event {} successfully sent", event.getId());
        } catch (InterruptedException | ExecutionException e) {
            log.error("⚠️ KAFKA FAILURE (Attempt {}): {}", event.getRetryCount(), e.getMessage());
            event.setRetryCount(event.getRetryCount() + 1);
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
        outboxRepository.save(event);
    }
}
