package com.thiagolima.desafio_backend_clube_do_Java.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User clientId;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private User freelancerId;

    private String title;
    private String description;
    private LocalDate deadline;

    @Column(name = "estimated_budget")
    private BigDecimal estimatedBudget;
    private ProjectStatus status;
}
