package com.thiagolima.desafio_backend_clube_do_Java.dto.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

class ProposalRequestTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-24T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        factory = Validation.byDefaultProvider().configure()
                .clockProvider(() -> CLOCK)
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void rejectsMissingOfferedValue() {
        assertViolation(new ProposalRequest(null, TODAY), "offeredValue", NotNull.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-2500.00"})
    void rejectsZeroAndNegativeOfferedValues(String value) {
        assertViolation(new ProposalRequest(new BigDecimal(value), TODAY),
                "offeredValue", Positive.class);
    }

    @Test
    void rejectsMissingEstimatedDeliveryDate() {
        assertViolation(new ProposalRequest(new BigDecimal("2500.00"), null),
                "estimatedDeliveryDate", NotNull.class);
    }

    @Test
    void rejectsPastEstimatedDeliveryDate() {
        assertViolation(new ProposalRequest(new BigDecimal("2500.00"), TODAY.minusDays(1)),
                "estimatedDeliveryDate", FutureOrPresent.class);
    }

    @Test
    void rejectsBothMissingFields() {
        var violations = validator.validate(new ProposalRequest(null, null));
        assertEquals(2, violations.size());
        assertEquals(java.util.Set.of("offeredValue", "estimatedDeliveryDate"),
                violations.stream().map(v -> v.getPropertyPath().toString())
                        .collect(java.util.stream.Collectors.toSet()));
        assertTrue(violations.stream().allMatch(v ->
                v.getConstraintDescriptor().getAnnotation().annotationType().equals(NotNull.class)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 30})
    void acceptsTodayAndFutureDeliveryDates(int daysFromToday) {
        var request = new ProposalRequest(new BigDecimal("2500.00"), TODAY.plusDays(daysFromToday));
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsSmallPositiveOfferedValue() {
        var request = new ProposalRequest(new BigDecimal("0.01"), TODAY);
        assertTrue(validator.validate(request).isEmpty());
    }

    private void assertViolation(ProposalRequest request, String field,
            Class<? extends Annotation> constraint) {
        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        var violation = violations.iterator().next();
        assertEquals(field, violation.getPropertyPath().toString());
        assertEquals(constraint, violation.getConstraintDescriptor().getAnnotation().annotationType());
    }
}
