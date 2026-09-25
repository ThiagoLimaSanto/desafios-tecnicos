package com.thiagolima.desafio_backend_clube_do_Java.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import com.thiagolima.desafio_backend_clube_do_Java.config.SecurityConfig;
import com.thiagolima.desafio_backend_clube_do_Java.config.SecurityFilter;
import com.thiagolima.desafio_backend_clube_do_Java.enums.*;
import com.thiagolima.desafio_backend_clube_do_Java.model.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.*;
import com.thiagolima.desafio_backend_clube_do_Java.service.*;

@WebMvcTest(ProjectCorrectionController.class)
@Import({SecurityConfig.class, SecurityFilter.class, ProjectCorrectionService.class})
class ProjectCorrectionControllerTest {
    private static final String URL = "/projects/10/corrections";
    private static final String BODY = "{\"correction\":\"Ajustar o layout\"}";

    @Autowired MockMvc mvc;
    @MockitoBean com.thiagolima.desafio_backend_clube_do_Java.outbox.OutboxService outboxService;
    @MockitoBean ProjectRepository projects;
    @MockitoBean ProjectCorrectionRepository corrections;
    @MockitoBean JwtService jwt;
    @MockitoBean UserDetailsService userDetails;
    private User client;
    private User freelancer;
    private Project project;

    @BeforeEach
    void setup() {
        client = account(1L, UserRole.CLIENT);
        freelancer = account(2L, UserRole.FREELANCER);
        project = new Project();
        ReflectionTestUtils.setField(project, "id", 10L);
        project.setClientId(client);
        project.setFreelancerId(freelancer);
        project.setStatus(ProjectStatus.COMPLETED);
        when(projects.findById(10L)).thenReturn(Optional.of(project));
        var correction = new ProjectCorrection();
        ReflectionTestUtils.setField(correction, "id", 20L);
        correction.setProject(project);
        correction.setCorrection("Ajustar o layout");
        var second = new ProjectCorrection();
        ReflectionTestUtils.setField(second, "id", 21L);
        second.setProject(project);
        second.setCorrection("Ajustar as cores");
        when(corrections.findByProjectId(10L)).thenReturn(List.of(correction, second));
    }

    @Test
    void clientAndAssignedFreelancerCanReadCorrections() throws Exception {
        for (User participant : List.of(client, freelancer)) {
            mvc.perform(get(URL).with(user(participant)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[1].correction").value("Ajustar as cores"))
                    .andExpect(jsonPath("$[0].id").value(20))
                    .andExpect(jsonPath("$[0].correction").value("Ajustar o layout"));
        }
    }

    @Test
    void outsidersCannotReadCorrections() throws Exception {
        for (UserRole role : List.of(UserRole.CLIENT, UserRole.FREELANCER)) {
            mvc.perform(get(URL).with(user(account(3L, role))))
                    .andExpect(status().isForbidden());
        }
        verify(corrections, never()).findByProjectId(anyLong());
    }

    @Test
    void projectWithoutFreelancerAllowsOwnerAndDeniesOutsider() throws Exception {
        project.setFreelancerId(null);
        mvc.perform(get(URL).with(user(client))).andExpect(status().isOk());
        mvc.perform(get(URL).with(user(freelancer))).andExpect(status().isForbidden());
    }

    @Test
    void ownerCreatesCorrectionAndReopensCompletedProject() throws Exception {
        mvc.perform(post(URL).with(user(client)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        var saved = ArgumentCaptor.forClass(ProjectCorrection.class);
        verify(corrections).save(saved.capture());
        assertSame(project, saved.getValue().getProject());
        assertEquals("Ajustar o layout", saved.getValue().getCorrection());
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        verify(projects).save(project);
    }

    @Test
    void freelancerCannotCreateCorrection() throws Exception {
        mvc.perform(post(URL).with(user(freelancer)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(projects);
        verify(corrections, never()).save(any());
    }

    @Test
    void anotherClientCannotCreateCorrection() throws Exception {
        mvc.perform(post(URL).with(user(account(3L, UserRole.CLIENT)))
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        verifyNoSaves();
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = "COMPLETED", mode = EnumSource.Mode.EXCLUDE)
    void incompatibleStatusReturnsConflictWithoutSaving(ProjectStatus status) throws Exception {
        project.setStatus(status);
        mvc.perform(post(URL).with(user(client)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
        assertEquals(status, project.getStatus());
        verifyNoSaves();
    }

    @Test
    void missingProjectReturnsNotFound() throws Exception {
        when(projects.findById(10L)).thenReturn(Optional.empty());
        mvc.perform(get(URL).with(user(client))).andExpect(status().isNotFound());
        mvc.perform(post(URL).with(user(client)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
        verifyNoSaves();
    }

    @Test
    void blankCorrectionReturnsBadRequest() throws Exception {
        mvc.perform(post(URL).with(user(client)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"correction\":\" \"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(projects);
        verify(corrections, never()).save(any());
    }

    @Test
    void anonymousRequestsReturnUnauthorized() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(projects);
    }

    private void verifyNoSaves() {
        verify(projects, never()).save(any());
        verify(corrections, never()).save(any());
    }

    private User account(Long id, UserRole role) {
        var account = new User();
        ReflectionTestUtils.setField(account, "id", id);
        account.setRole(role);
        account.setEmail("user" + id + "@example.com");
        return account;
    }
}
