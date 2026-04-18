package app.vercel.dev_portfolio.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "profile")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Profile {
    @Id
    private Long id;

    private String name;
    private String headline;

    @Column(name = "sub_headline", columnDefinition = "TEXT")
    private String subHeadline;

    @Column(name = "resume_url")
    private String resumeUrl;

    private String email;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;
}