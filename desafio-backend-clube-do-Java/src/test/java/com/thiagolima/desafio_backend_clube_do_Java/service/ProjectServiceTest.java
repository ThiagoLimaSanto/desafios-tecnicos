package com.thiagolima.desafio_backend_clube_do_Java.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.exception.*;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.outbox.OutboxService;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import java.util.List;

class ProjectServiceTest {
    private final ProjectRepository projects = mock(ProjectRepository.class);
    private final OutboxService outbox = mock(OutboxService.class);
    private final ProjectService service = new ProjectService(projects, outbox);

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new User(), null, List.of()));
    }
    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    @Test
    void completingOpenProjectThrowsStateConflictInsteadOfNotFound() {
        Project project = new Project();
        project.setStatus(ProjectStatus.OPEN);
        when(projects.findById(1L)).thenReturn(Optional.of(project));
        assertThrows(ProjectNotInProgressException.class, () -> service.completedProject(1L));
        verify(projects, never()).save(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void finishingInProgressProjectThrowsStateConflictInsteadOfNotFound() {
        Project project = new Project();
        project.setStatus(ProjectStatus.IN_PROGRESS);
        when(projects.findById(1L)).thenReturn(Optional.of(project));
        assertThrows(ProjectNotCompletedException.class, () -> service.finishedProject(1L));
        verify(projects, never()).save(any());
        verifyNoInteractions(outbox);
    }
}
