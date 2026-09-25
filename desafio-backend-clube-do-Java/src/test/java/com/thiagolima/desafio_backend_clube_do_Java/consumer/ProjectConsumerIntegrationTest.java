package com.thiagolima.desafio_backend_clube_do_Java.consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.thiagolima.desafio_backend_clube_do_Java.config.RabbitMQConfig;
import com.thiagolima.desafio_backend_clube_do_Java.enums.*;
import com.thiagolima.desafio_backend_clube_do_Java.event.*;
import com.thiagolima.desafio_backend_clube_do_Java.model.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.*;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationService;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationRecipientResolver;
import com.thiagolima.desafio_backend_clube_do_Java.service.PendingNotificationService;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/consumer-test.properties",
        "spring.jpa.hibernate.ddl-auto=create-drop"
}, showSql = false)
@Import({ProjectConsumer.class, NotificationService.class, NotificationRecipientResolver.class,
        PendingNotificationService.class, RabbitMQConfig.class,
        ProjectConsumerIntegrationTest.ListenerConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProjectConsumerIntegrationTest {
    @Autowired NotificationRepository notifications;
    @Autowired PendingNotificationRepository pending;
    @Autowired ProposalRepository proposals;
    @Autowired ProjectRepository projects;
    @Autowired UserRepository users;
    @Autowired RabbitListenerEndpointRegistry registry;
    @Autowired MessageConverter converter;
    @Autowired ProjectConsumer consumer;
    User client;
    User freelancer;
    Project project;

    @BeforeEach
    void setup() {
        pending.deleteAll();
        notifications.deleteAll();
        proposals.deleteAll();
        projects.deleteAll();
        users.deleteAll();
        client = users.save(new User());
        freelancer = users.save(new User());
        project = new Project();
        project.setClientId(client);
        project.setStatus(ProjectStatus.OPEN);
        // No assigned freelancer: rejected and negotiated proposals still have a recipient in the event.
        project = projects.save(project);
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void consumesJsonAndHeaderAndPersistsNotificationForCorrectRecipient(NotificationType type) throws Exception {
        verifyDelivery(type, false);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationType.class, names = {"PROPOSAL_ACCEPTED", "PROPOSAL_REJECTED",
            "PROPOSAL_NEGOTIATED", "PROJECT_CORRECTIONS", "PROJECT_FINISHED"})
    void legacyEventWithoutFreelancerIsResolved(NotificationType type) throws Exception {
        verifyDelivery(type, true);
    }

    private void verifyDelivery(NotificationType type, boolean legacy) throws Exception {
        UUID eventId = UUID.randomUUID();
        Long recipientInEvent = legacy ? null : freelancer.getId();
        Proposal proposal = new Proposal();
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        proposal.setStatus(type == NotificationType.PROPOSAL_REJECTED
                ? ProposalStatus.REJECTED : ProposalStatus.ACCEPTED);
        proposals.save(proposal);
        Object event;
        String queue;
        Long recipient = freelancer.getId();
        switch (type) {
            case PROPOSAL_ACCEPTED -> {
                event = new ProjectAcceptEvent(project.getId(), client.getId(), recipientInEvent);
                queue = RabbitMQConfig.PROJECT_ACCEPTED_QUEUE;
            }
            case PROPOSAL_REJECTED -> {
                event = new ProjectRejectedEvent(project.getId(), client.getId(), recipientInEvent);
                queue = RabbitMQConfig.PROJECT_REJECTED_QUEUE;
            }
            case PROPOSAL_NEGOTIATED -> {
                event = new ProjectNegotiateEvent(project.getId(), proposal.getId(), recipientInEvent);
                queue = RabbitMQConfig.PROPOSAL_NEGOTIATED_QUEUE;
            }
            case PROJECT_CORRECTIONS -> {
                event = new ProjectCorrectionsEvent(project.getId(), recipientInEvent);
                queue = RabbitMQConfig.PROJECT_CORRECTIONS_QUEUE;
            }
            case PROJECT_FINISHED -> {
                event = new ProjectFinishEvent(project.getId(), client.getId(), recipientInEvent);
                queue = RabbitMQConfig.PROJECT_FINISHED_QUEUE;
            }
            case PROJECT_COMPLETED -> {
                event = new ProjectCompletEvent(project.getId(), client.getId());
                queue = RabbitMQConfig.PROJECT_COMPLETED_QUEUE;
                recipient = client.getId();
            }
            case PROPOSAL_CREATED -> {
                event = new ProposalEvent(project.getId(), client.getId(), freelancer.getId());
                queue = RabbitMQConfig.PROPOSAL_QUEUE;
                recipient = client.getId();
            }
            default -> throw new AssertionError(type);
        }
        MessageProperties properties = new MessageProperties();
        properties.setHeader("eventId", eventId.toString());
        var message = converter.toMessage(event, properties);
        var container = registry.getListenerContainers().stream()
                .map(SimpleMessageListenerContainer.class::cast)
                .filter(c -> java.util.Arrays.asList(c.getQueueNames()).contains(queue))
                .findFirst().orElseThrow();
        var listener = (ChannelAwareMessageListener) container.getMessageListener();
        listener.onMessage(message, null);
        listener.onMessage(message, null); // Redelivery uses the same event ID.

        assertEquals(1, notifications.count());
        var notification = notifications.findByEventId(eventId).orElseThrow();
        assertEquals(recipient, notification.getUser().getId());
        assertEquals(project.getId(), notification.getProject().getId());
        assertEquals(type, notification.getType());
        assertFalse(notification.getMessage().isBlank());
        assertNotNull(notification.getCreatedAt());
        assertNull(notification.getReadAt());
    }

    @Test
    void concurrentRedeliveriesCreateOnlyOneNotification() {
        UUID eventId = UUID.randomUUID();
        var event = new ProjectRejectedEvent(project.getId(), client.getId(), freelancer.getId());
        var deliveries = IntStream.range(0, 8)
                .mapToObj(i -> CompletableFuture.runAsync(() -> consumer.reject(event, eventId)))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(deliveries).orTimeout(10, java.util.concurrent.TimeUnit.SECONDS).join();
        assertEquals(1, notifications.count());
    }

    @Test
    void ambiguousRejectionIsPreservedAndCanBeReplayedWithCorrectRecipient() {
        User other = users.save(new User());
        for (User user : java.util.List.of(freelancer, other)) {
            Proposal proposal = new Proposal();
            proposal.setProject(project);
            proposal.setFreelancer(user);
            proposal.setStatus(ProposalStatus.REJECTED);
            proposals.save(proposal);
        }
        UUID eventId = UUID.randomUUID();
        var legacy = new ProjectRejectedEvent(project.getId(), client.getId(), null);
        consumer.reject(legacy, eventId);
        consumer.reject(legacy, eventId);
        assertEquals(0, notifications.count());
        assertEquals(1, pending.count());
        var saved = pending.findById(eventId).orElseThrow();
        assertEquals(project.getId(), saved.getProjectId());
        assertEquals(NotificationType.PROPOSAL_REJECTED, saved.getType());
        assertNotNull(saved.getReason());
        consumer.reject(new ProjectRejectedEvent(project.getId(), client.getId(), freelancer.getId()), eventId);
        assertEquals(1, notifications.count());
        assertEquals(freelancer.getId(), notifications.findByEventId(eventId).orElseThrow().getUser().getId());
        assertFalse(pending.existsById(eventId));
    }

    @Test
    void negotiationFromAnotherProjectDoesNotChooseItsFreelancer() {
        Project other = new Project();
        other.setStatus(ProjectStatus.OPEN);
        projects.save(other);
        Proposal proposal = new Proposal();
        proposal.setProject(other);
        proposal.setFreelancer(freelancer);
        proposals.save(proposal);
        UUID eventId = UUID.randomUUID();
        consumer.negotiated(new ProjectNegotiateEvent(project.getId(), proposal.getId(), null), eventId);
        assertEquals(0, notifications.count());
        assertTrue(pending.existsById(eventId));
    }

    @TestConfiguration
    @EnableRabbit
    static class ListenerConfiguration {
        @Bean
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(MessageConverter converter) {
            var factory = new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(mock(ConnectionFactory.class));
            factory.setMessageConverter(converter);
            factory.setAutoStartup(false);
            return factory;
        }
    }
}
