package app.vercel.dev_portfolio.portfolio.integration.controller;


import app.vercel.dev_portfolio.portfolio.dto.LoginRequest;
import app.vercel.dev_portfolio.portfolio.entity.Profile;
import app.vercel.dev_portfolio.portfolio.entity.User;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import app.vercel.dev_portfolio.portfolio.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminShouldBeAbleToUpdateProfile() throws Exception {
        // Setup user
        userRepository.deleteAll();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User admin = User.builder()
                .email("ajay@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build();
        userRepository.save(admin);

        // Login
        LoginRequest login = new LoginRequest("ajay@example.com", "admin123");
        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = loginResponse.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        String token = jsonNode.get("token").asText();

        // Now perform admin request
        Profile updatedProfile = Profile.builder()
                .name("Ajay Nair")
                .headline("Cloud Architect")
                .subHeadline("Senior Full Stack")
                .build();

        mockMvc.perform(put("/api/v1/admin/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProfile)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Cloud Architect"));
    }

    @Test
    void anonymousUserShouldBeForbiddenFromAdmin() throws Exception {
        Profile profile = new Profile();

        mockMvc.perform(put("/api/v1/admin/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isForbidden()); // JWT filter will reject this
    }
}
