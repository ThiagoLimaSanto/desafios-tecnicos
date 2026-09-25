package com.thiagolima.desafio_backend_clube_do_Java.event;

public record ProjectFinishEvent(Long projectId, Long clientId, Long freelancerId) {
}
