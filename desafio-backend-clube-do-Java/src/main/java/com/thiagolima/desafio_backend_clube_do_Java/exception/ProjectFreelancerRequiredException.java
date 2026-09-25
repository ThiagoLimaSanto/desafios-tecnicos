package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class ProjectFreelancerRequiredException extends AccessDeniedException {
    public ProjectFreelancerRequiredException(String message) {
        super(message);
    }
}
