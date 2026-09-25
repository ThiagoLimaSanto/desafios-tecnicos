package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_pending", columnList = "publishedAt,nextAttemptAt,createdAt"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String exchangeName;
    @Column(nullable = false)
    private String routingKey;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant nextAttemptAt;
    private Instant publishedAt;
    private int attempts;
    @Column(length = 1000)
    private String lastError;

    public OutboxEvent(String exchangeName, String routingKey, String payload, String eventType) {
        this.id = UUID.randomUUID();
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
        this.eventType = eventType;
        this.createdAt = Instant.now();
        this.nextAttemptAt = createdAt;
    }

    public void published() {
        attempts++;
        publishedAt = Instant.now();
        lastError = null;
    }

    public void retryLater(Exception error) {
        attempts++;
        nextAttemptAt = Instant.now().plusSeconds(30);
        String description = error.toString();
        lastError = description.substring(0, Math.min(description.length(), 1000));
    }
}
