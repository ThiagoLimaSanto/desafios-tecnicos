package com.thiagolima.desafio_backend_clube_do_Java.outbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.thiagolima.desafio_backend_clube_do_Java.service.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.*;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.enums.UserRole;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.ArgumentCaptor;

import com.thiagolima.desafio_backend_clube_do_Java.config.RabbitMQConfig;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectAcceptEvent;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/outbox-test.properties",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "outbox.confirm-timeout-ms=10"
}, showSql = false)
@Import({OutboxService.class, OutboxPublisher.class, RabbitMQConfig.class,
        ProposalService.class, ProjectService.class, ProjectCorrectionService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OutboxIntegrationTest {
    @Autowired ProposalService proposalService;
    @Autowired ProjectService projectService;
    @Autowired ProjectCorrectionService correctionService;
    @Autowired ProposalRepository proposals;
    @Autowired ProjectCorrectionRepository corrections;
    @Autowired UserRepository users;
    @Autowired OutboxRepository events;
    @Autowired ProjectRepository projects;
    @Autowired OutboxService outbox;
    @Autowired OutboxPublisher publisher;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired MessageConverter converter;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean RabbitTemplate rabbit;
    TransactionTemplate transaction;

    @BeforeEach
    void setup() {
        events.deleteAll();
        corrections.deleteAll();
        proposals.deleteAll();
        projects.deleteAll();
        users.deleteAll();
        transaction = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeBusinessLifecyclePersistsAllEventsWithoutBroker() {
        User client = new User();
        client.setRole(UserRole.CLIENT);
        users.save(client);
        User freelancer = new User();
        freelancer.setRole(UserRole.FREELANCER);
        users.save(freelancer);
        Project project = new Project();
        project.setStatus(ProjectStatus.OPEN);
        project.setClientId(client);
        projects.save(project);
        Long id = project.getId();
        var request = new ProposalRequest(BigDecimal.TEN, LocalDate.now().plusDays(5));

        authenticate(freelancer);
        proposalService.applyProject(id, request);
        Long first = proposals.findAll().getFirst().getId();
        authenticate(client);
        proposalService.negotiate(id, first);
        proposalService.reject(id, first);
        authenticate(freelancer);
        proposalService.applyProject(id, request);
        Long second = proposals.findAll().stream().filter(p -> !p.getId().equals(first))
                .findFirst().orElseThrow().getId();
        authenticate(client);
        proposalService.accept(id, second);
        authenticate(freelancer);
        projectService.completedProject(id);
        authenticate(client);
        correctionService.corrections(id, new CorrectionsRequest("Ajustar layout"));
        authenticate(freelancer);
        projectService.completedProject(id);
        authenticate(client);
        projectService.finishedProject(id);

        assertEquals(ProjectStatus.FINISHED, projects.findById(id).orElseThrow().getStatus());
        assertEquals(9, events.count());
        assertEquals(7, events.findAll().stream().map(OutboxEvent::getRoutingKey).distinct().count());
        assertTrue(events.findAll().stream().allMatch(e -> e.getPublishedAt() == null));
        verifyNoInteractions(rabbit);
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void commitsBusinessChangeAndEventTogetherWithoutCallingBroker() {
        transaction.executeWithoutResult(status -> {
            Project project = new Project();
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projects.save(project);
            enqueue();
        });
        assertEquals(1, projects.count());
        assertEquals(1, events.count());
        verifyNoInteractions(rabbit);
    }

    @Test
    void rollbackRemovesBothBusinessChangeAndEvent() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            Project project = new Project();
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projects.saveAndFlush(project);
            enqueue();
            events.flush();
            throw new IllegalStateException("Failure after persisting both records");
        }));
        assertEquals(0, projects.count());
        assertEquals(0, events.count());
        verifyNoInteractions(rabbit);
    }

    @Test
    void enqueueRequiresBusinessTransaction() {
        assertThrows(IllegalTransactionStateException.class, this::enqueue);
        assertEquals(0, events.count());
    }

    @Test
    void publisherCannotSeeUncommittedEvent() throws Exception {
        transaction.executeWithoutResult(status -> {
            enqueue();
            events.flush();
            assertFalse(CompletableFuture.supplyAsync(publisher::publishNext).join());
            status.setRollbackOnly();
        });
        verifyNoInteractions(rabbit);
    }

    @Test
    void ackMarksPublishedAndPreservesJsonTypeAndStableMessageId() {
        persistEvent();
        acknowledge(true);
        assertTrue(publisher.publishNext());
        var event = events.findAll().getFirst();
        assertNotNull(event.getPublishedAt());
        assertEquals(1, event.getAttempts());
        var message = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq(RabbitMQConfig.PROJECT_EXCHANGE),
                eq(RabbitMQConfig.PROJECT_ACCEPTED_KEY), message.capture(), any(CorrelationData.class));
        assertEquals(event.getId().toString(), message.getValue().getMessageProperties().getMessageId());
        assertEquals(MessageDeliveryMode.PERSISTENT, message.getValue().getMessageProperties().getDeliveryMode());
        assertEquals(new ProjectAcceptEvent(10L, 20L, 30L), converter.fromMessage(message.getValue()));
        assertFalse(publisher.publishNext());
    }

    @Test
    void nackRetainsEventForRetry() {
        persistEvent();
        acknowledge(false);
        publisher.publishNext();
        assertPendingRetry();
        assertTrue(events.findAll().getFirst().getLastError().contains("EventPublishRejectedException"));
    }

    @Test
    void unroutableMessageRemainsPendingEvenWithAck() {
        persistEvent();
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            correlation.setReturned(new ReturnedMessage(call.getArgument(2), 312,
                    "NO_ROUTE", RabbitMQConfig.PROJECT_EXCHANGE, RabbitMQConfig.PROJECT_ACCEPTED_KEY));
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publishNext();
        assertPendingRetry();
        assertTrue(events.findAll().getFirst().getLastError().contains("UnroutableEventException"));
    }

    @Test
    void timeoutRetainsEventForRetry() {
        persistEvent();
        publisher.publishNext();
        assertPendingRetry();
    }

    @Test
    void brokerFailureThenRecoveryReusesSameEventId() {
        persistEvent();
        doThrow(new IllegalStateException("Broker unavailable")).when(rabbit)
                .send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publishNext();
        assertPendingRetry();
        var original = events.findAll().getFirst();
        jdbc.update("UPDATE outbox_events SET next_attempt_at = ? WHERE id = ?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(1)), original.getId());
        acknowledge(true);
        publisher.publishNext();
        var event = events.findById(original.getId()).orElseThrow();
        assertNotNull(event.getPublishedAt());
        assertEquals(2, event.getAttempts());
        var messages = ArgumentCaptor.forClass(Message.class);
        verify(rabbit, times(2)).send(anyString(), anyString(), messages.capture(), any(CorrelationData.class));
        assertEquals(messages.getAllValues().get(0).getMessageProperties().getMessageId(),
                messages.getAllValues().get(1).getMessageProperties().getMessageId());
    }

    @Test
    void concurrentPublisherSkipsLockedEvent() {
        persistEvent();
        transaction.executeWithoutResult(status -> {
            assertTrue(events.lockNextPending(Instant.now()).isPresent());
            assertFalse(CompletableFuture.supplyAsync(publisher::publishNext)
                    .orTimeout(2, TimeUnit.SECONDS).join());
        });
        verifyNoInteractions(rabbit);
    }

    private void enqueue() {
        outbox.enqueue(RabbitMQConfig.PROJECT_EXCHANGE, RabbitMQConfig.PROJECT_ACCEPTED_KEY,
                new ProjectAcceptEvent(10L, 20L, 30L));
    }

    private void persistEvent() {
        transaction.executeWithoutResult(status -> enqueue());
    }

    private void acknowledge(boolean ack) {
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(ack, ack ? null : "Rejected"));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
    }

    private void assertPendingRetry() {
        var event = events.findAll().getFirst();
        assertNull(event.getPublishedAt());
        assertEquals(1, event.getAttempts());
        assertNotNull(event.getLastError());
        assertTrue(event.getNextAttemptAt().isAfter(Instant.now()));
        assertFalse(publisher.publishNext());
    }
}
