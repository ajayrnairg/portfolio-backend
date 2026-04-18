package app.vercel.dev_portfolio.portfolio.integration.controller;


import app.vercel.dev_portfolio.portfolio.dto.LoginRequest;
import app.vercel.dev_portfolio.portfolio.entity.*;
import app.vercel.dev_portfolio.portfolio.integration.base.BaseIntegrationTest;
import app.vercel.dev_portfolio.portfolio.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
class AdminControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ContactRepository contactRepository;

    private String adminToken;

    @BeforeEach
    void setUpAdminUser() throws Exception {
        userRepository.deleteAll();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User admin = User.builder()
                .email("ajay@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build();
        userRepository.save(admin);

        LoginRequest login = new LoginRequest("ajay@example.com", "admin123");
        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        adminToken = jsonNode.get("token").asText();
    }

    @Test
    void anonymousUserShouldBeForbiddenFromAdmin() throws Exception {
        Profile profile = new Profile();
        mockMvc.perform(put("/api/v1/admin/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldBeAbleToUpdateProfile() throws Exception {
        Profile updatedProfile = Profile.builder()
                .name("Ajay Nair")
                .headline("Cloud Architect")
                .subHeadline("Senior Full Stack")
                .build();

        mockMvc.perform(put("/api/v1/admin/profile")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProfile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Cloud Architect"));
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void adminShouldBeAbleToCrudAboutSection() throws Exception {
        // Ensure row with ID 1 exists to bypass Hibernate detached entity exception
        jdbcTemplate.execute("MERGE INTO about_section (id, title, description) KEY(id) VALUES (1, 't', 'd')");
        
        AboutSection section = new AboutSection();
        section.setTitle("About me");
        section.setDescription("Hello");

        mockMvc.perform(put("/api/v1/admin/about/section")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(section)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("About me"));
    }

    @Test
    void adminShouldBeAbleToCrudExperience() throws Exception {
        Experience exp = new Experience();
        exp.setTitle("Software Engineer");
        exp.setStage("2020-2022");
        exp.setDescription("Backend.");

        MvcResult res = mockMvc.perform(post("/api/v1/admin/experience")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        exp.setTitle("Senior Engineer");
        mockMvc.perform(put("/api/v1/admin/experience/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/experience/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminShouldBeAbleToCrudProjects() throws Exception {
        Project proj = new Project();
        proj.setTitle("Cool App");

        MvcResult res = mockMvc.perform(post("/api/v1/admin/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proj)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        proj.setTitle("Cooler App");
        mockMvc.perform(put("/api/v1/admin/projects/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proj)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/projects/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminShouldBeAbleToCrudAwardsAndCertifications() throws Exception {
        Awards award = new Awards();
        award.setTitle("Best Dev");
        MvcResult awdRes = mockMvc.perform(post("/api/v1/admin/awards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(award)))
                .andReturn();
        Long awardId = objectMapper.readTree(awdRes.getResponse().getContentAsString()).get("id").asLong();
        
        mockMvc.perform(put("/api/v1/admin/awards/" + awardId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(award)))
                .andExpect(status().isOk());
                
        mockMvc.perform(delete("/api/v1/admin/awards/" + awardId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        Certifications cert = new Certifications();
        cert.setTitle("AWS Certified");
        MvcResult certRes = mockMvc.perform(post("/api/v1/admin/certifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cert)))
                .andReturn();
        Long certId = objectMapper.readTree(certRes.getResponse().getContentAsString()).get("id").asLong();
        
        mockMvc.perform(put("/api/v1/admin/certifications/" + certId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cert)))
                .andExpect(status().isOk());
                
        mockMvc.perform(delete("/api/v1/admin/certifications/" + certId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminShouldBeAbleToCrudSkills() throws Exception {
        SkillCategory category = new SkillCategory();
        category.setCategoryName("Backend");

        MvcResult catRes = mockMvc.perform(post("/api/v1/admin/skill-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andReturn();
        Long catId = objectMapper.readTree(catRes.getResponse().getContentAsString()).get("id").asLong();

        category.setCategoryName("Frontend");
        mockMvc.perform(put("/api/v1/admin/skill-categories/" + catId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk());

        Skill skill = new Skill();
        skill.setSkillName("Java");

        MvcResult skillRes = mockMvc.perform(post("/api/v1/admin/skill-categories/" + catId + "/skills")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skill)))
                .andExpect(status().isOk())
                .andReturn();
        Long skillId = objectMapper.readTree(skillRes.getResponse().getContentAsString()).get("id").asLong();

        skill.setSkillName("Python");
        mockMvc.perform(put("/api/v1/admin/skills/" + skillId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skill)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/skills/" + skillId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin/skill-categories/" + catId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminShouldBeAbleToManageContactSubmissions() throws Exception {
        ContactMessage msg = new ContactMessage();
        msg.setName("Test Sender");
        msg.setEmail("test@ex.com");
        msg.setSubject("Test");
        msg.setMessage("Long enough message for testing.");
        contactRepository.save(msg);

        mockMvc.perform(get("/api/v1/admin/contact-submissions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(post("/api/v1/admin/contact-submissions/bulk-delete")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(msg.getId()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminShouldBeAbleToUpdateContactInfo() throws Exception {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setEmail("old@ex.com");
        profileRepository.save(profile);
        
        Map<String, String> updates = Map.of(
            "email", "new@example.com",
            "githubUrl", "http://github.com/new"
        );
        mockMvc.perform(patch("/api/v1/admin/profile/contact-info")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.githubUrl").value("http://github.com/new"));
    }
}
