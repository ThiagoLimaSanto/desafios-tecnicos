package com.thiagolima.desafio_backend_clube_do_Java.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/notification-test.properties",
        "spring.jpa.hibernate.ddl-auto=create-drop"
}, showSql = false)
class NotificationTest {
    @Autowired EntityManager entityManager;

    @Test
    void persistsRelationshipsEventTypeAndReadTimestamp() {
        Notification notification = notification(UUID.randomUUID());
        entityManager.persist(notification);
        entityManager.flush();
        Long id = notification.getId();
        Long userId = notification.getUser().getId();
        Long projectId = notification.getProject().getId();
        Instant createdAt = notification.getCreatedAt();
        assertNotNull(createdAt);
        assertNull(notification.getReadAt());
        entityManager.clear();

        Notification saved = entityManager.find(Notification.class, id);
        assertEquals(userId, saved.getUser().getId());
        assertEquals(projectId, saved.getProject().getId());
        assertEquals(notification.getEventId(), saved.getEventId());
        assertEquals(notification.getMessage(), saved.getMessage());
        assertEquals("PROPOSAL_ACCEPTED", entityManager.createNativeQuery(
                "SELECT type FROM notifications WHERE id = :id", String.class)
                .setParameter("id", id).getSingleResult());
        Instant readAt = Instant.parse("2026-09-25T12:00:00Z");
        saved.setReadAt(readAt);
        entityManager.flush();
        entityManager.clear();
        saved = entityManager.find(Notification.class, id);
        assertEquals(readAt, saved.getReadAt());
        assertEquals(createdAt, saved.getCreatedAt());
    }

    @Test
    void rejectsDuplicateEventForSameRecipient() {
        Notification first = notification(UUID.randomUUID());
        entityManager.persist(first);
        entityManager.flush();
        Notification duplicate = new Notification();
        duplicate.setUser(first.getUser());
        duplicate.setProject(first.getProject());
        duplicate.setType(first.getType());
        duplicate.setMessage(first.getMessage());
        duplicate.setEventId(first.getEventId());
        entityManager.persist(duplicate);
        assertThrows(PersistenceException.class, entityManager::flush);
    }

    @Test
    void rejectsSameEventEvenForDifferentRecipients() {
        UUID eventId = UUID.randomUUID();
        entityManager.persist(notification(eventId));
        entityManager.persist(notification(eventId));
        assertThrows(PersistenceException.class, entityManager::flush);
    }

    private Notification notification(UUID eventId) {
        User user = new User();
        entityManager.persist(user);
        Project project = new Project();
        project.setStatus(ProjectStatus.OPEN);
        entityManager.persist(project);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setProject(project);
        notification.setType(NotificationType.PROPOSAL_ACCEPTED);
        notification.setMessage("Sua proposta foi aceita. ".repeat(20));
        notification.setEventId(eventId);
        return notification;
    }
}
