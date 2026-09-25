package com.thiagolima.desafio_backend_clube_do_Java.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalHandlerExceptionsTest {
    @ParameterizedTest
    @MethodSource("errors")
    void mapsSpecificErrorsToStatusAndStructuredBody(RuntimeException error, int status) throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ErrorController(error))
                .setControllerAdvice(new GlobalHandlerExceptions()).build();
        mvc.perform(get("/test/error"))
                .andExpect(status().is(status))
                .andExpect(jsonPath("status").value(status))
                .andExpect(jsonPath("message").value(error.getMessage()))
                .andExpect(jsonPath("timestamp").exists());
    }

    static Stream<Arguments> errors() {
        return Stream.of(
                Arguments.of(new ProjectOwnerRequiredException("Somente o dono"), 403),
                Arguments.of(new ProjectFreelancerRequiredException("Freelancer incorreto"), 403),
                Arguments.of(new ProjectParticipantRequiredException("Acesso restrito aos participantes"), 403),
                Arguments.of(new FreelancerRoleRequiredException("Perfil freelancer obrigatório"), 403),
                Arguments.of(new AccountOwnerRequiredException("Conta de outro usuário"), 403),
                Arguments.of(new ProposalProjectMismatchException("Proposta de outro projeto"), 403),
                Arguments.of(new ProposalNotFoundException("Proposta não encontrada"), 404),
                Arguments.of(new ProjectNotFoundException("Projeto não encontrado"), 404),
                Arguments.of(new UserNotExistException("Usuário não encontrado"), 404),
                Arguments.of(new ProjectNotOpenException("Projeto fechado"), 409),
                Arguments.of(new ProjectNotInProgressException("Projeto não está em andamento"), 409),
                Arguments.of(new ProjectNotCompletedException("Projeto não está concluído"), 409),
                Arguments.of(new ProposalNegotiationNotAllowedException("Negociação não permitida"), 409),
                Arguments.of(new ProposalDecisionNotAllowedException("Decisão não permitida"), 409),
                Arguments.of(new DuplicateNotificationException("Notificação duplicada"), 409),
                Arguments.of(new ProjectExistFreelancerException("Freelancer já associado"), 409),
                Arguments.of(new UserExistException("Email já cadastrado"), 409),
                Arguments.of(new UserRoleRequiredException("Perfil é obrigatório"), 400));
    }

    @RestController
    static class ErrorController {
        private final RuntimeException error;
        ErrorController(RuntimeException error) { this.error = error; }
        @GetMapping("/test/error")
        void fail() { throw error; }
    }
}
