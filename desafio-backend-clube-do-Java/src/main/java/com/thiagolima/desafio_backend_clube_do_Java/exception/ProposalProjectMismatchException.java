package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class ProposalProjectMismatchException extends AccessDeniedException {
    public ProposalProjectMismatchException(String message) {
        super(message);
    }
}
