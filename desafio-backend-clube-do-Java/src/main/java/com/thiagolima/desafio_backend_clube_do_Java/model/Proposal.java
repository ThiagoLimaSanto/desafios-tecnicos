package com.thiagolima.desafio_backend_clube_do_Java.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProposalStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proposals")
@NoArgsConstructor
@Getter
@Setter
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private ProposalStatus status = ProposalStatus.PENDING;

    private BigDecimal offeredValue;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
}
