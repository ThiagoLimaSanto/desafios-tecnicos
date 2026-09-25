package com.thiagolima.desafio_backend_clube_do_Java.event;

public record ProposalEvent(Long projectId, Long clientId, Long freelancerId) {
}
