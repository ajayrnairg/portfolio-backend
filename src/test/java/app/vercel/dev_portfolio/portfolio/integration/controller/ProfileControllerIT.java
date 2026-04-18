package app.vercel.dev_portfolio.portfolio.integration.controller;

import app.vercel.dev_portfolio.portfolio.entity.Profile;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import app.vercel.dev_portfolio.portfolio.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc // This allows @Autowired MockMvc to work
class ProfileControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();

        Profile profile = Profile.builder()
                .id(1L)
                .name("Ajay Nair")
                .headline("Scalable Architecture")
                .subHeadline("Full Stack Dev")
                .resumeUrl("http://resume.pdf")
                .build();

        profileRepository.save(profile);
    }

    @Test
    void shouldReturnProfileData() throws Exception {
        mockMvc.perform(get("/api/v1/profile/getProfile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Ajay Nair")))
                .andExpect(jsonPath("$.headline", is("Scalable Architecture")));
    }

    @Test
    void shouldTrackAndRedirectToResume() throws Exception {
        mockMvc.perform(get("/api/v1/profile/resume/download"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "dummy"));
    }

    @Test
    void shouldReturnDownloadStats() throws Exception {
        // Trigger a download to populate stats
        mockMvc.perform(get("/api/v1/profile/resume/download"));

        mockMvc.perform(get("/api/v1/profile/resume/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloads").isArray())
                .andExpect(jsonPath("$.downloads.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.downloads[0].ipAddress", is("127.0.0.1")));
    }
}