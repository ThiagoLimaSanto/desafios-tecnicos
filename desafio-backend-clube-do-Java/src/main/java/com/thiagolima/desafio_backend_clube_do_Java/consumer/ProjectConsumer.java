package com.thiagolima.desafio_backend_clube_do_Java.consumer;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.thiagolima.desafio_backend_clube_do_Java.config.RabbitMQConfig;
import com.thiagolima.desafio_backend_clube_do_Java.dto.notification.NotificationRequest;
import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import com.thiagolima.desafio_backend_clube_do_Java.event.*;
import com.thiagolima.desafio_backend_clube_do_Java.exception.DuplicateNotificationException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.InvalidNotificationEventException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserNotExistException;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.UserRepository;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationService;
import com.thiagolima.desafio_backend_clube_do_Java.service.NotificationRecipientResolver;
import com.thiagolima.desafio_backend_clube_do_Java.service.PendingNotificationService;
import com.thiagolima.desafio_backend_clube_do_Java.model.PendingNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectConsumer {
    private final NotificationService notificationService;
    private final ProjectRepository projects;
    private final UserRepository users;
    private final NotificationRecipientResolver recipientResolver;
    private final PendingNotificationService pendingNotifications;

    @RabbitListener(queues = RabbitMQConfig.PROJECT_ACCEPTED_QUEUE)
    public void accept(ProjectAcceptEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.freelancerId(), NotificationType.PROPOSAL_ACCEPTED,
                "Sua proposta foi aceita.", eventId);
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_REJECTED_QUEUE)
    public void reject(ProjectRejectedEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.freelancerId(), NotificationType.PROPOSAL_REJECTED,
                "Sua proposta foi rejeitada.", eventId);
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_CORRECTIONS_QUEUE)
    public void corrections(ProjectCorrectionsEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.freelancerId(), NotificationType.PROJECT_CORRECTIONS,
                "O cliente solicitou correções no projeto.", eventId);
    }

    @RabbitListener(queues = RabbitMQConfig.PROPOSAL_NEGOTIATED_QUEUE)
    public void negotiated(ProjectNegotiateEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.freelancerId(), NotificationType.PROPOSAL_NEGOTIATED,
                "O cliente iniciou uma negociação sobre sua proposta.", eventId, event.proposalId());
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_COMPLETED_QUEUE)
    public void completed(ProjectCompletEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.ClientId(), NotificationType.PROJECT_COMPLETED,
                "O freelancer concluiu o projeto. Revise a entrega.", eventId);
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_FINISHED_QUEUE)
    public void finished(ProjectFinishEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.freelancerId(), NotificationType.PROJECT_FINISHED,
                "O cliente aprovou a entrega e finalizou o projeto.", eventId);
    }

    @RabbitListener(queues = RabbitMQConfig.PROPOSAL_QUEUE)
    public void proposalCreated(ProposalEvent event, @Header("eventId") UUID eventId) {
        notify(event.projectId(), event.clientId(), NotificationType.PROPOSAL_CREATED,
                "Você recebeu uma nova proposta para o projeto.", eventId);
    }

    private void notify(Long projectId, Long recipientId, NotificationType type, String message, UUID eventId) {
        notify(projectId, recipientId, type, message, eventId, null);
    }

    private void notify(Long projectId, Long recipientId, NotificationType type, String message,
            UUID eventId, Long proposalId) {
        if (eventId == null || projectId == null) {
            throw new InvalidNotificationEventException("Evento de notificação sem identificador ou projeto");
        }
        if (notificationService.hasNotificationForEvent(eventId)) {
            pendingNotifications.removeResolved(eventId);
            return;
        }
        var project = projects.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto da notificação não encontrado"));
        if (recipientId == null) recipientId = recipientResolver.resolve(project, proposalId, type);
        if (recipientId == null) {
            pendingNotifications.preserve(new PendingNotification(eventId, projectId, proposalId, type, message));
            log.warn("Evento {} preservado em pending_notifications: destinatário não identificável", eventId);
            return;
        }
        var recipient = users.findById(recipientId)
                .orElseThrow(() -> new UserNotExistException("Destinatário da notificação não encontrado"));
        try {
            notificationService.createNotification(new NotificationRequest(recipient, project, type, message, eventId));
        } catch (DuplicateNotificationException duplicate) {
            log.debug("Evento {} já possui notificação", eventId);
        } catch (DataIntegrityViolationException error) {
            if (!notificationService.hasNotificationForEvent(eventId)) throw error;
            log.debug("Evento {} processado por outro consumidor", eventId);
        }
        pendingNotifications.removeResolved(eventId);
    }
}
