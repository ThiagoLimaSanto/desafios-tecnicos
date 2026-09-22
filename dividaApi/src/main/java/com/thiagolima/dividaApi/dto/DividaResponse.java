package com.thiagolima.dividaApi.dto;

import java.math.BigDecimal;

import com.thiagolima.dividaApi.model.Divida;

public record DividaResponse(
        Long id,
        String cpfDevedor,
        BigDecimal valorPego,
        BigDecimal valorComJuros,
        BigDecimal valorComDesconto) {
    public DividaResponse convertToDividaResponse(Divida divida) {
        return new DividaResponse(divida.getId(), divida.getCpfDevedor(), divida.getValorPego(),
                divida.getValorComJuros(),
                divida.getValorComDesconto());
    }
}
