package com.resurrection_finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    private String eventType;
    private String aggregateId;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastAttemptAt;
    private boolean processed = false;
    private int retryCount;
}