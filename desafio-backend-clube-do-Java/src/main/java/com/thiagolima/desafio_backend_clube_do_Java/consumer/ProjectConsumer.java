package com.thiagolima.desafio_backend_clube_do_Java.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.thiagolima.desafio_backend_clube_do_Java.config.RabbitMQConfig;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectAcceptEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectCompletEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectCorrectionsEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectFinishEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectNegotiateEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProjectRejectedEvent;
import com.thiagolima.desafio_backend_clube_do_Java.event.ProposalEvent;

@Component
public class ProjectConsumer {

    @RabbitListener(queues = RabbitMQConfig.PROJECT_ACCEPTED_QUEUE)
    public void accept(ProjectAcceptEvent event) {
        System.out.println("ProjectAcceptEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_REJECTED_QUEUE)
    public void reject(ProjectRejectedEvent event) {
        System.out.println("ProjectRejectedEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_CORRECTIONS_QUEUE)
    public void corrections(ProjectCorrectionsEvent event) {
        System.out.println("ProjectCorrectionsEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROPOSAL_NEGOTIATED_QUEUE)
    public void negotiated(ProjectNegotiateEvent event) {
        System.out.println("ProjectNegotiateEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_COMPLETED_QUEUE)
    public void completed(ProjectCompletEvent event) {
        System.out.println("ProjectCompletedEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROJECT_FINISHED_QUEUE)
    public void finished(ProjectFinishEvent event) {
        System.out.println("ProjectFinishedEvent");
    }

    @RabbitListener(queues = RabbitMQConfig.PROPOSAL_QUEUE)
    public void proposalCreated(ProposalEvent event) {
        System.out.println("proposalCreatedEvent");
    }
}
