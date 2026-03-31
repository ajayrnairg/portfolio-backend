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
}