package app.vercel.dev_portfolio.portfolio.integration.controller;

import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql("/test-data-about.sql")
@ActiveProfiles("test")
public class AboutControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllTabs() throws Exception {
        mockMvc.perform(get("/api/v1/about"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tabs", hasSize(4)))
                .andExpect(jsonPath("$.tabs[?(@.title == 'experience')].info[0].title")
                        .value("Full Stack Software Developer | ISS-STOXX"));
    }
}
