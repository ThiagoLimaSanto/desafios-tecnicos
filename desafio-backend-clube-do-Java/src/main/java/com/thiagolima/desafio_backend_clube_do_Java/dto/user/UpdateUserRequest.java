package com.thiagolima.desafio_backend_clube_do_Java.dto.user;

import com.thiagolima.desafio_backend_clube_do_Java.enums.DocumentType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Nome é obrigatório") String document,
        @NotNull(message = "Tipo de documento é obrigatório") DocumentType documentType,
        @NotBlank(message = "Nome é obrigatório") @Email(message = "Email inválido") String email,
        @NotBlank(message = "Nome é obrigatório") String password) {
}
