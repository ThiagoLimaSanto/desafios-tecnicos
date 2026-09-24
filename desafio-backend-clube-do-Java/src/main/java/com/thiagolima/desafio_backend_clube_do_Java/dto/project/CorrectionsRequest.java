package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import jakarta.validation.constraints.NotBlank;

public record CorrectionsRequest(@NotBlank(message = "Adicione o texto da correção") String correction) {

}
