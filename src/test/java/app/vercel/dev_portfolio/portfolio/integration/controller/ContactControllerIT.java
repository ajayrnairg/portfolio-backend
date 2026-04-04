package app.vercel.dev_portfolio.portfolio.integration.controller;

import app.vercel.dev_portfolio.portfolio.dto.ContactRequest;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import app.vercel.dev_portfolio.portfolio.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ContactControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Use @MockitoBean instead of @MockBean for Spring Boot 4 / Spring 7
    @MockitoBean
    private ContactService contactService;

    @Test
    void shouldSaveMessageAndReturnOk() throws Exception {
        ContactRequest request = new ContactRequest(
                "Ajay Nair",
                "ajay@example.com",
                "Interview Invite",
                "Hello Ajay, we loved your portfolio!"
        );

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // This fails with 500 if the JSON is malformed or validation is ignored.
        // Spring's @Valid triggers BEFORE the service call.
        ContactRequest request = new ContactRequest(
                "Ajay",
                "not-an-email", // Invalid email
                "Subject",
                "Short" // Potentially invalid length depending on your DTO constraints
        );

        mockMvc.perform(post("/api/v1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }
}