package com.thiagolima.dividaApi.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DividaRequest(
        @NotBlank(message = "CPF Deve ser preenchido") @CPF(message = "CPF Deve ser um CPF valido") String cpfDevedor,

        @NotNull(message = "Valor Pego Deve ser preenchido") @Positive(message = "Valor Pego Deve ser positivo") BigDecimal valorPego,

        @NotNull(message = "Valor Com Juros Deve ser preenchido") @Positive(message = "Valor Com Juros Deve ser positivo") BigDecimal valorComJuros,

        @NotNull(message = "Valor Com Desconto Deve ser preenchido") @Positive(message = "Valor Com Desconto Deve ser positivo") BigDecimal valorComDesconto) {
}
