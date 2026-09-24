package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProposalRequest(
        @NotNull(message = "O valor oferecido é obrigatório") @Positive(message = "O valor oferecido deve ser positivo") BigDecimal offeredValue,
        @NotNull(message = "A data de entrega estimada é obrigatória") @FutureOrPresent(message = "A data de entrega estimada deve ser futura ou presente") LocalDate estimatedDeliveryDate) {
}
