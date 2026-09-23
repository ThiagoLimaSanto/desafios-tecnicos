package com.thiagolima.desafio_backend_clube_do_Java.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;

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
@Table(name = "projects")
@NoArgsConstructor
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(value = AccessLevel.NONE)
    private Long id;

    private String title;
    private String description;
    private LocalDate deadline;

    @Column(name = "estimated_budget")
    private BigDecimal estimatedBudget;
    private ProjectStatus status;
}
