package com.thiagolima.desafio_backend_clube_do_Java.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROJECT_EXCHANGE = "projects.events";

    public static final String PROJECT_COMPLETED_QUEUE = "projects.completed";
    public static final String PROJECT_COMPLETED_KEY = "project.completed";

    public static final String PROJECT_FINISHED_QUEUE = "projects.finished";
    public static final String PROJECT_FINISHED_KEY = "project.finished";

    public static final String PROJECT_ACCEPTED_QUEUE = "projects.accepted";
    public static final String PROJECT_ACCEPTED_KEY = "project.accepted";

    public static final String PROJECT_REJECTED_QUEUE = "projects.rejected";
    public static final String PROJECT_REJECTED_KEY = "project.rejected";

    public static final String PROPOSAL_NEGOTIATED_QUEUE = "proposals.negotiated";
    public static final String PROPOSAL_NEGOTIATED_KEY = "proposal.negotiated";

    public static final String PROJECT_CORRECTIONS_QUEUE = "projects.corrections";
    public static final String PROJECT_CORRECTIONS_KEY = "project.corrections";

    public static final String PROPOSAL_QUEUE = "proposals.created";
    public static final String PROPOSAL_CREATED_KEY = "proposal.created";

    @Bean
    public DirectExchange projectExchange() {
        return new DirectExchange(PROJECT_EXCHANGE);
    }

    @Bean
    Queue projectCompletedQueue() {
        return QueueBuilder.durable(PROJECT_COMPLETED_QUEUE).build();
    }

    @Bean
    Binding projectCompletedBinding(@Qualifier("projectCompletedQueue") Queue projectCompletedQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(projectCompletedQueue)
                .to(projectExchange)
                .with(PROJECT_COMPLETED_KEY);
    }

    @Bean
    Queue projectFinishedQueue() {
        return QueueBuilder.durable(PROJECT_FINISHED_QUEUE).build();
    }

    @Bean
    Binding projectFinishedBinding(@Qualifier("projectFinishedQueue") Queue projectFinishedQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(projectFinishedQueue)
                .to(projectExchange)
                .with(PROJECT_FINISHED_KEY);
    }

    @Bean
    Queue projectAcceptedQueue() {
        return QueueBuilder.durable(PROJECT_ACCEPTED_QUEUE).build();
    }

    @Bean
    Binding projectAcceptedBinding(@Qualifier("projectAcceptedQueue") Queue projectAcceptedQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(projectAcceptedQueue)
                .to(projectExchange)
                .with(PROJECT_ACCEPTED_KEY);
    }

    @Bean
    Queue projectRejectedQueue() {
        return QueueBuilder.durable(PROJECT_REJECTED_QUEUE).build();
    }

    @Bean
    Binding projectRejectedBinding(@Qualifier("projectRejectedQueue") Queue projectRejectedQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(projectRejectedQueue)
                .to(projectExchange)
                .with(PROJECT_REJECTED_KEY);
    }

    @Bean
    Queue projectCorrectionsQueue() {
        return QueueBuilder.durable(PROJECT_CORRECTIONS_QUEUE).build();
    }

    @Bean
    Binding projectCorrectionsBinding(@Qualifier("projectCorrectionsQueue") Queue projectCorrectionsQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(projectCorrectionsQueue)
                .to(projectExchange)
                .with(PROJECT_CORRECTIONS_KEY);
    }

    @Bean
    Queue proposalNegotiatedQueue() {
        return QueueBuilder.durable(PROPOSAL_NEGOTIATED_QUEUE).build();
    }

    @Bean
    Binding proposalNegotiatedBinding(@Qualifier("proposalNegotiatedQueue") Queue proposalNegotiatedQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(proposalNegotiatedQueue)
                .to(projectExchange)
                .with(PROPOSAL_NEGOTIATED_KEY);
    }

    @Bean
    Queue proposalQueue() {
        return QueueBuilder.durable(PROPOSAL_QUEUE).build();
    }

    @Bean
    Binding proposalBinding(@Qualifier("proposalQueue") Queue proposalQueue,
            @Qualifier("projectExchange") DirectExchange projectExchange) {
        return BindingBuilder.bind(proposalQueue)
                .to(projectExchange)
                .with(PROPOSAL_CREATED_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("com.thiagolima.desafio_backend_clube_do_Java.event");
    }
}
