package com.thiagolima.desafio_backend_clube_do_Java.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagolima.desafio_backend_clube_do_Java.config.*;
import com.thiagolima.desafio_backend_clube_do_Java.controller.*;
import com.thiagolima.desafio_backend_clube_do_Java.enums.UserRole;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.UserRepository;
import com.thiagolima.desafio_backend_clube_do_Java.service.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@WebMvcTest({UserController.class, ProjectController.class})
@Import({SecurityConfig.class, SecurityFilter.class, JwtService.class, UserDetailsService.class, UserService.class})
@TestPropertySource(properties = "jwt.secret=abababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababab")
class SecurityFlowTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired PasswordEncoder encoder;
    @MockitoBean UserRepository users;
    @MockitoBean ProjectService projects;
    User client;

    @BeforeEach
    void prepare() {
        client = user(1L, "client@example.com", UserRole.CLIENT);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(users.findById(1L)).thenReturn(Optional.of(client));
        when(projects.getProjects()).thenReturn(List.of());
    }

    private User user(Long id, String email, UserRole role) {
        var user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(email);
        user.setName("Test");
        user.setRole(role);
        user.setPassword(encoder.encode("old-password"));
        return user;
    }

    private String bearer(User user) {
        return "Bearer " + jwt.generateToken(user.getEmail(), user.getRole(), user.getName());
    }

    private String updateBody() {
        return """
                {"name":"Updated","document":"123","documentType":"CPF",
                 "email":"client@example.com","password":"new-password"}
                """;
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void registrationPersistsSelectedRoleAndHashedPassword(UserRole role) throws Exception {
        mvc.perform(post("/users/create").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Test","document":"123","documentType":"CPF",
                 "email":"new@example.com","password":"secret","role":"%s"}
                """.formatted(role))).andExpect(status().isCreated());
        var saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(role);
        assertThat(encoder.matches("secret", saved.getValue().getPassword())).isTrue();
        assertThat(saved.getValue().getAuthorities()).extracting("authority").containsExactly("ROLE_" + role);
    }

    @Test
    void registrationRequiresRole() throws Exception {
        mvc.perform(post("/users/create").contentType(MediaType.APPLICATION_JSON).content(updateBody()))
                .andExpect(status().isBadRequest());
        verify(users, never()).save(any());
    }

    @Test
    void loginUsesUserDetailsAndReturnsToken() throws Exception {
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                .content(""" 
                        {"email":"client@example.com","password":"old-password"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"client@example.com", "missing@example.com"})
    void invalidCredentialsReturn401(String email) throws Exception {
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void projectsRequireAuthentication() throws Exception {
        mvc.perform(get("/projects/get")).andExpect(status().isUnauthorized());
        verifyNoInteractions(projects);
    }

    @Test
    void clientCanReadProjects() throws Exception {
        mvc.perform(get("/projects/get").header("Authorization", bearer(client))).andExpect(status().isOk());
    }

    @Test
    void freelancerCannotAccessProjectSubpaths() throws Exception {
        var freelancer = user(2L, "freelancer@example.com", UserRole.FREELANCER);
        when(users.findByEmail(freelancer.getEmail())).thenReturn(Optional.of(freelancer));
        mvc.perform(get("/projects/get").header("Authorization", bearer(freelancer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/projects/create").header("Authorization", bearer(freelancer)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(projects);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer broken", "Bearer "})
    void malformedTokensReturn401(String header) throws Exception {
        mvc.perform(get("/projects/get").header("Authorization", header)).andExpect(status().isUnauthorized());
        verifyNoInteractions(projects);
    }

    @Test
    void expiredAndWrongSignatureTokensReturn401() throws Exception {
        var key = Keys.hmacShaKeyFor(HexFormat.of().parseHex("ab".repeat(64)));
        var expired = Jwts.builder().subject(client.getEmail()).expiration(new Date(1000)).signWith(key).compact();
        var wrongSignature = new JwtService("cd".repeat(64)).generateToken(client.getEmail(), UserRole.CLIENT, "Test");
        for (var token : List.of(expired, wrongSignature)) {
            mvc.perform(get("/projects/get").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(projects);
    }

    @Test
    void tokenWithoutSubjectReturns401() throws Exception {
        String token = jwt.generateToken("", UserRole.CLIENT, "Test");
        mvc.perform(get("/projects/get").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(projects);
    }

    @Test
    void tokenForRemovedUserReturns401() throws Exception {
        var token = bearer(client);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.empty());
        mvc.perform(get("/projects/get").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void legacyNullRoleDoesNotGrantProjectAccessOrCrash() throws Exception {
        client.setRole(null);
        mvc.perform(get("/projects/get").header("Authorization", bearer(client))).andExpect(status().isForbidden());
    }

    @Test
    void cannotModifyAnotherAccount() throws Exception {
        mvc.perform(put("/users/update/2").header("Authorization", bearer(client))
                .contentType(MediaType.APPLICATION_JSON).content(updateBody())).andExpect(status().isForbidden());
        mvc.perform(delete("/users/delete/2").header("Authorization", bearer(client)))
                .andExpect(status().isForbidden());
        verify(users, never()).findById(2L);
        verify(users, never()).save(any());
        verify(users, never()).delete(any(User.class));
    }

    @Test
    void canUpdateOwnAccountAndLoginWithNewPassword() throws Exception {
        mvc.perform(put("/users/update/1").header("Authorization", bearer(client))
                .contentType(MediaType.APPLICATION_JSON).content(updateBody())).andExpect(status().isOk());
        assertThat(encoder.matches("new-password", client.getPassword())).isTrue();
        assertThat(client.getRole()).isEqualTo(UserRole.CLIENT);
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"client@example.com","password":"new-password"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void canDeleteOwnAccount() throws Exception {
        mvc.perform(delete("/users/delete/1").header("Authorization", bearer(client))).andExpect(status().isOk());
        verify(users).delete(client);
    }

    @Test
    void getCannotDeleteAccount() throws Exception {
        mvc.perform(get("/users/delete/1").header("Authorization", bearer(client)))
                .andExpect(status().isMethodNotAllowed());
        verify(users, never()).delete(any(User.class));
    }
}
