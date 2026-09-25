package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.desafio_backend_clube_do_Java.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByEventId(UUID eventId);
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
