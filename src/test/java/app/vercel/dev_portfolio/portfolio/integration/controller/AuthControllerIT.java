package app.vercel.dev_portfolio.portfolio.integration.controller;


import app.vercel.dev_portfolio.portfolio.dto.LoginRequest;
import app.vercel.dev_portfolio.portfolio.entity.User;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import app.vercel.dev_portfolio.portfolio.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupUser() {
        userRepository.deleteAll();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User admin = User.builder()
                .email("ajay@example.com")
                .password(passwordEncoder.encode("admin123")) // Use real encoder
                .role("ROLE_ADMIN")
                .build();
        userRepository.save(admin);
    }

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        // Ensure you have an @Sql script or BeforeEach that saves this user to H2
        LoginRequest login = new LoginRequest("ajay@example.com", "admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
