package com.thiagolima.desafio_backend_clube_do_Java.dto.notification;

import java.util.UUID;

import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;

public record NotificationRequest(
    User user,
    Project project,
    NotificationType type,
    String message,
    UUID eventId
) {
    
}
