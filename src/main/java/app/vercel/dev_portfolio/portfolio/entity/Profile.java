package app.vercel.dev_portfolio.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "profile")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String headline;

    @Column(name = "sub_headline", columnDefinition = "TEXT")
    private String subHeadline;

    @Column(name = "resume_url")
    private String resumeUrl;
}