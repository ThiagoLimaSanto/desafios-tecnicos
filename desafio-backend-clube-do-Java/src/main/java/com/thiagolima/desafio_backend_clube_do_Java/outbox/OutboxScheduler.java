package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxPublisher publisher;

    @Scheduled(fixedDelayString = "${outbox.poll-delay-ms:1000}")
    public void publishPending() {
        for (int i = 0; i < 50 && !Thread.currentThread().isInterrupted(); i++) {
            if (!publisher.publishNext()) break;
        }
    }
}
