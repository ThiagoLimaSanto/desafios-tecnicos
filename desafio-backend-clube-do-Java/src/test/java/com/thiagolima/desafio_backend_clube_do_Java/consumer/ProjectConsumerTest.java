package com.thiagolima.desafio_backend_clube_do_Java.consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.thiagolima.desafio_backend_clube_do_Java.exception.InvalidNotificationEventException;
import org.springframework.dao.DataIntegrityViolationException;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectRejectedEvent;
import com.thiagolima.desafio_backend_clube_do_Java.exception.DuplicateNotificationException;
import com.thiagolima.desafio_backend_clube_do_Java.model.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.*;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationService;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationRecipientResolver;
import com.thiagolima.desafio_backend_clube_do_Java.service.PendingNotificationService;

class ProjectConsumerTest {
    final NotificationService notifications = mock(NotificationService.class);
    final ProjectRepository projects = mock(ProjectRepository.class);
    final UserRepository users = mock(UserRepository.class);
    final NotificationRecipientResolver resolver = mock(NotificationRecipientResolver.class);
    final PendingNotificationService pending = mock(PendingNotificationService.class);
    final ProjectConsumer consumer = new ProjectConsumer(notifications, projects, users, resolver, pending);
    final UUID id = UUID.randomUUID();
    final ProjectRejectedEvent event = new ProjectRejectedEvent(1L, 2L, 3L);

    void setupEntities() {
        when(projects.findById(1L)).thenReturn(Optional.of(new Project()));
        when(users.findById(3L)).thenReturn(Optional.of(new User()));
    }

    @Test
    void ignoresEventAlreadyProcessedWithoutLoadingDeletedEntities() {
        when(notifications.hasNotificationForEvent(id)).thenReturn(true);
        consumer.reject(event, id);
        verifyNoInteractions(projects, users);
        verify(notifications, never()).createNotification(any());
    }

    @Test
    void duplicateNotificationIsAcknowledged() {
        setupEntities();
        doThrow(new DuplicateNotificationException("Duplicada")).when(notifications).createNotification(any());
        assertDoesNotThrow(() -> consumer.reject(event, id));
    }

    @Test
    void concurrentInsertConflictIsAcknowledgedOnlyWhenEventExists() {
        setupEntities();
        when(notifications.hasNotificationForEvent(id)).thenReturn(false, true);
        doThrow(new DataIntegrityViolationException("Unique event")).when(notifications).createNotification(any());
        assertDoesNotThrow(() -> consumer.reject(event, id));
    }

    @Test
    void unrelatedDatabaseFailureIsPropagatedForRetry() {
        setupEntities();
        var failure = new DataIntegrityViolationException("Foreign key");
        doThrow(failure).when(notifications).createNotification(any());
        assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> consumer.reject(event, id)));
    }

    @Test
    void missingProjectIsRejected() {
        assertThrows(InvalidNotificationEventException.class,
                () -> consumer.reject(new ProjectRejectedEvent(null, 2L, 3L), id));
        verifyNoInteractions(notifications, projects, users);
    }

    @Test
    void unresolvedRecipientIsPreservedWithoutNotifyingClient() {
        setupEntities();
        when(resolver.resolve(any(), isNull(), any())).thenReturn(null);
        consumer.reject(new ProjectRejectedEvent(1L, 2L, null), id);
        verify(pending).preserve(any(PendingNotification.class));
        verify(notifications, never()).createNotification(any());
        verifyNoInteractions(users);
    }

    @Test
    void pendingStorageFailureIsPropagatedSoMessageCanBeRetried() {
        setupEntities();
        when(resolver.resolve(any(), isNull(), any())).thenReturn(null);
        var error = new DataIntegrityViolationException("Storage unavailable");
        doThrow(error).when(pending).preserve(any());
        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> consumer.reject(new ProjectRejectedEvent(1L, 2L, null), id)));
        verify(notifications, never()).createNotification(any());
    }
}
