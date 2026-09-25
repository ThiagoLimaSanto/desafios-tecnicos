package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class ProjectOwnerRequiredException extends AccessDeniedException {
    public ProjectOwnerRequiredException(String message) {
        super(message);
    }
}
