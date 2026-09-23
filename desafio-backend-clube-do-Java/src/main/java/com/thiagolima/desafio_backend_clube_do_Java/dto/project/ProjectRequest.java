package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProjectRequest(

                @NotBlank(message = "Título é obrigatório") String title,

                @NotBlank(message = "Descrição é obrigatória") String description,

                @NotNull(message = "Prazo é obrigatório") @FutureOrPresent(message = "Prazo deve ser uma data futura") LocalDate deadline,

                @NotNull(message = "Orçamento estimado é obrigatório") @Positive(message = "Orçamento estimado deve ser maior que zero") BigDecimal estimatedBudget

) {
}
