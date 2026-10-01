package com.reelixen.splithelp.auth;

import com.jayway.jsonpath.JsonPath;
import com.reelixen.splithelp.config.SecurityConfig;
import com.reelixen.splithelp.user.User;
import com.reelixen.splithelp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class, LoginIntegrationTest.ProbeController.class})
class LoginIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean UserRepository userRepository;
    @MockitoBean AuthService authService;

    @BeforeEach
    void setUp() {
        User user = new User("Nazar", "nazar@example.com", passwordEncoder.encode("TestPassword123!"));
        when(userRepository.findByEmail("nazar@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void loginPersistsAuthenticationRotatesSessionAndCsrfToken() throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) csrf.getRequest().getSession(false);
        String originalId = session.getId();
        String token = JsonPath.read(csrf.getResponse().getContentAsString(), "$.token");

        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType("application/json").content(credentials("NAZAR@example.com", "TestPassword123!")))
                .andExpect(status().isNoContent()).andExpect(content().string(""));

        assertNotEquals(originalId, session.getId());
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull(context);
        assertEquals("nazar@example.com", context.getAuthentication().getName());
        assertNull(context.getAuthentication().getCredentials());
        mvc.perform(get("/test/current-user").session(session))
                .andExpect(status().isOk()).andExpect(content().string("nazar@example.com"));

        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType("application/json").content(credentials("nazar@example.com", "TestPassword123!")))
                .andExpect(status().isForbidden());
        MvcResult fresh = mvc.perform(get("/api/v1/auth/csrf").session(session)).andReturn();
        String freshToken = JsonPath.read(fresh.getResponse().getContentAsString(), "$.token");
        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", freshToken)
                        .contentType("application/json").content(credentials("nazar@example.com", "TestPassword123!")))
                .andExpect(status().isNoContent());
    }

    @Test
    void wrongPasswordAndUnknownEmailReturnSameError() throws Exception {
        for (String email : new String[]{"nazar@example.com", "missing@example.com"}) {
            MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
            MockHttpSession session = (MockHttpSession) csrf.getRequest().getSession(false);
            String token = JsonPath.read(csrf.getResponse().getContentAsString(), "$.token");
            mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                            .contentType("application/json").content(credentials(email, "WrongPassword!")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
            assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        }
    }

    @Test
    void loginWithoutCsrfIsRejectedBeforeUserLookup() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(credentials("nazar@example.com", "TestPassword123!")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(userRepository);
    }

    @Test
    void anonymousRequestToProtectedEndpointReturns401() throws Exception {
        mvc.perform(get("/test/current-user")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidLoginInputReturns400() throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        String token = JsonPath.read(csrf.getResponse().getContentAsString(), "$.token");
        mvc.perform(post("/api/v1/auth/login")
                        .session((MockHttpSession) csrf.getRequest().getSession(false))
                        .header("X-CSRF-TOKEN", token).contentType("application/json")
                        .content(credentials("invalid-email", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userRepository);
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @RestController
    static class ProbeController {
        @GetMapping("/test/current-user")
        String currentUser(Principal principal) {
            return principal.getName();
        }
    }
}
