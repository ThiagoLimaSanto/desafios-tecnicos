package com.thiagolima.desafio_backend_clube_do_Java.dto.user;

import com.thiagolima.desafio_backend_clube_do_Java.enums.DocumentType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Nome é obrigatório") String document,
        @NotBlank(message = "Nome é obrigatório") DocumentType documentType,
        @NotBlank(message = "Nome é obrigatório") @Email(message = "Email inválido") String email,
        @NotBlank(message = "Nome é obrigatório") String password) {
}
