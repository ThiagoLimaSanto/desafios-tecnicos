package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository repository;
    private final MessageConverter converter;

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String exchange, String routingKey, Object event) {
        var message = converter.toMessage(event, new MessageProperties());
        repository.save(new OutboxEvent(exchange, routingKey,
                new String(message.getBody(), StandardCharsets.UTF_8), event.getClass().getName()));
    }
}
