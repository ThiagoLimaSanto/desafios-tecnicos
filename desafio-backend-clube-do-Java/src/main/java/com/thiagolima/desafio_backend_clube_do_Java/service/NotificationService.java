package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import com.thiagolima.desafio_backend_clube_do_Java.dto.notification.NotificationRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.notification.NotificationResponse;
import com.thiagolima.desafio_backend_clube_do_Java.exception.DuplicateNotificationException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.NotificationNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.model.Notification;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.NotificationRepository;
import com.thiagolima.desafio_backend_clube_do_Java.utils.GetUserAuthentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        if (notificationRepository.findByEventId(request.eventId()).isPresent())
            throw new DuplicateNotificationException("Notificação já criada para este evento");
        notification.setUser(request.user());
        notification.setProject(request.project());
        notification.setType(request.type());
        notification.setMessage(request.message());
        notification.setEventId(request.eventId());

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public boolean hasNotificationForEvent(UUID eventId) {
        return notificationRepository.findByEventId(eventId).isPresent();
    }

    public void markAsRead(Long id) {
        User user = GetUserAuthentication.getUserAuthenticated();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada"));
        if (!Objects.equals(user.getId(), notification.getUser().getId())) {
            throw new NotificationNotFoundException("Você não pode marcar esta notificação como lida");
        }
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> listNotifications() {
        User user = GetUserAuthentication.getUserAuthenticated();
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(notification -> new NotificationResponse(notification.getId(), notification.getUser(),
                        notification.getProject(), notification.getType(), notification.getMessage(),
                        notification.getCreatedAt(), notification.getReadAt()))
                .toList();
    }
}
