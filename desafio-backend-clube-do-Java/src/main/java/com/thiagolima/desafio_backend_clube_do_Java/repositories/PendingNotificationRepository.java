package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.thiagolima.desafio_backend_clube_do_Java.model.PendingNotification;

public interface PendingNotificationRepository extends JpaRepository<PendingNotification, UUID> {
}
