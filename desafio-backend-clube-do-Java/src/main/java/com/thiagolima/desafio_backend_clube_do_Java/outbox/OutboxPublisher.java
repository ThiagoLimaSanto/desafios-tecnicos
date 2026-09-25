package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    @Value("${outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs = 5000;

    @Transactional
    public boolean publishNext() {
        var pending = repository.lockNextPending(Instant.now());
        if (pending.isEmpty()) return false;
        OutboxEvent event = pending.get();
        try {
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding(StandardCharsets.UTF_8.name());
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setMessageId(event.getId().toString());
            properties.setHeader("eventId", event.getId().toString());
            properties.setHeader("__TypeId__", event.getEventType());
            Message message = new Message(event.getPayload().getBytes(StandardCharsets.UTF_8), properties);
            // Correlation IDs identify attempts; messageId identifies the logical event across retries.
            CorrelationData correlation = new CorrelationData();
            rabbitTemplate.send(event.getExchangeName(), event.getRoutingKey(), message, correlation);
            var confirm = correlation.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ did not confirm routing: " + confirm.reason());
            }
            event.published();
        } catch (Exception error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            event.retryLater(error);
            log.warn("Outbox event {} remains pending: {}", event.getId(), error.toString());
        }
        // JPA dirty checking persists the result while the row is still locked.
        return true;
    }
}
