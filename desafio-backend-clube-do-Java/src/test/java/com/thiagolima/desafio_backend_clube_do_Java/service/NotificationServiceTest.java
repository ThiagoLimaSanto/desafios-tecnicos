package com.thiagolima.desafio_backend_clube_do_Java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.thiagolima.desafio_backend_clube_do_Java.dto.notification.NotificationRequest;
import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import com.thiagolima.desafio_backend_clube_do_Java.exception.DuplicateNotificationException;
import com.thiagolima.desafio_backend_clube_do_Java.model.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.NotificationRepository;

class NotificationServiceTest {
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final NotificationService service = new NotificationService(repository);

    @Test
    void duplicateForSameRecipientThrowsSpecificExceptionWithoutSaving() {
        var request = request(1L, UUID.randomUUID());
        when(repository.findByEventId(request.eventId())).thenReturn(Optional.of(new Notification()));
        assertThrows(DuplicateNotificationException.class, () -> service.createNotification(request));
        verify(repository, never()).save(any());
    }

    @Test
    void newEventCreatesNotification() {
        UUID eventId = UUID.randomUUID();
        when(repository.findByEventId(eventId)).thenReturn(Optional.empty());
        service.createNotification(request(2L, eventId));
        verify(repository).findByEventId(eventId);
        verify(repository).save(any(Notification.class));
    }

    private NotificationRequest request(Long userId, UUID eventId) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        return new NotificationRequest(user, new Project(), NotificationType.PROPOSAL_ACCEPTED,
                "Proposta aceita", eventId);
    }
}
