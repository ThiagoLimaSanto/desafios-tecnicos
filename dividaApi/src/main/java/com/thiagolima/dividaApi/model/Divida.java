
package com.thiagolima.dividaApi.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "divida")
@NoArgsConstructor
@Getter
@Setter
public class Divida {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "cpf_devedor")
    private String cpfDevedor;

    @Column(name = "nome_devedor")
    private BigDecimal valorPego;

    @Column(name = "valor_pego")
    private BigDecimal valorComJuros;

    private BigDecimal valorComDesconto;
}
