package com.thiagolima.desafio_backend_clube_do_Java.model;

import java.time.Instant;
import java.util.UUID;
import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pending_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingNotification {
    @Id
    private UUID eventId;
    private Long projectId;
    private Long proposalId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @Column(nullable = false, columnDefinition = "text")
    private String message;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String reason;

    public PendingNotification(UUID eventId, Long projectId, Long proposalId,
            NotificationType type, String message) {
        this.eventId = eventId;
        this.projectId = projectId;
        this.proposalId = proposalId;
        this.type = type;
        this.message = message;
        this.createdAt = Instant.now();
        this.reason = "Destinatário ausente ou ambíguo no evento antigo";
    }
}
