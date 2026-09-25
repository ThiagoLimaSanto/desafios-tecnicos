package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    // PostgreSQL: concurrent publishers claim different rows until their transactions finish.
    @Query(value = """
            SELECT * FROM outbox_events
            WHERE published_at IS NULL AND next_attempt_at <= :now
            ORDER BY created_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<OutboxEvent> lockNextPending(@Param("now") Instant now);
}
