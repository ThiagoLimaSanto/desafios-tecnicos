package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class ProjectParticipantRequiredException extends AccessDeniedException {
    public ProjectParticipantRequiredException(String message) {
        super(message);
    }
}
