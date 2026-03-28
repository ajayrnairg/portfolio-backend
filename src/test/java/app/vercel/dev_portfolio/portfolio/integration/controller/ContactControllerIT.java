package app.vercel.dev_portfolio.portfolio.integration.controller;

import app.vercel.dev_portfolio.portfolio.dto.ContactRequest;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureJson
@Import(ContactControllerIT.TestConfig.class)
@ActiveProfiles("test")
class ContactControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }
    }

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
}
