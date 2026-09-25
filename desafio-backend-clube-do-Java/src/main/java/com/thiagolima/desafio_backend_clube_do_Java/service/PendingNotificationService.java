package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.thiagolima.desafio_backend_clube_do_Java.model.PendingNotification;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.PendingNotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingNotificationService {
    private final PendingNotificationRepository repository;

    @Transactional
    public void preserve(PendingNotification event) {
        if (!repository.existsById(event.getEventId())) repository.save(event);
    }

    @Transactional
    public void removeResolved(UUID eventId) {
        repository.deleteById(eventId);
    }
}
