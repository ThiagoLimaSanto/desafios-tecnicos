package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String title,
    String description,
    LocalDate deadline,
    BigDecimal estimatedBudget
) {
    
}
