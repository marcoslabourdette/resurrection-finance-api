package com.resurrection_finance.scheduler;
import com.resurrection_finance.entity.OutboxEvent;
import com.resurrection_finance.repository.OutboxRepository;
import com.resurrection_finance.service.OutboxService;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final OutboxService outboxService;
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pending = outboxRepository.findTop20ByProcessedFalseAndRetryCountLessThan(5);
        for (OutboxEvent event : pending) {
            try {
                outboxService.processSingleEvent(event);
            } catch (Exception e) {
                log.error("❌ Critical failure in event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}