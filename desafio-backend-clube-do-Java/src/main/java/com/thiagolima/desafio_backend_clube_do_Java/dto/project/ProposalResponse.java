package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thiagolima.desafio_backend_clube_do_Java.enums.ProposalStatus;

public record ProposalResponse(
        Long id,
        Long freelancerId,
        String freelancerName,
        BigDecimal offeredValue,
        LocalDate estimatedDeliveryDate,
        ProposalStatus status
) {

}
