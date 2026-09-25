package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProposalNotFoundException extends RuntimeException {
    public ProposalNotFoundException(String message) {
        super(message);
    }
}
