package com.thiagolima.desafio_backend_clube_do_Java.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LoginUserRequest(
        @NotBlank(message = "Email é obrigatório") String email,

        @NotBlank(message = "Senha é obrigatório") String password) {

}
