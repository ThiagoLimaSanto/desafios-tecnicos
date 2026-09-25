package com.thiagolima.desafio_backend_clube_do_Java.event;

public record ProjectCompletEvent(
        Long projectId,
        Long ClientId) {
}