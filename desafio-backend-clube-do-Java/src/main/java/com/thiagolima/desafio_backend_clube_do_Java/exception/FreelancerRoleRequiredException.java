package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class FreelancerRoleRequiredException extends AccessDeniedException {
    public FreelancerRoleRequiredException(String message) {
        super(message);
    }
}
