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
@Sql("/test-data-work.sql")
@ActiveProfiles("test")
class WorkControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnFullWorkSectionData() throws Exception {
        mockMvc.perform(get("/api/v1/work"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Featured Engineering"))
                .andExpect(jsonPath("$.projects").exists())
                .andExpect(jsonPath("$.projects[0].ctaLinks", hasSize(4)))
                .andExpect(jsonPath("$.projects[0].techStack").isArray());
    }
}