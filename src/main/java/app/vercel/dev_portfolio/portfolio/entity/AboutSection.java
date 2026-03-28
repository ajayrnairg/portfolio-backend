package app.vercel.dev_portfolio.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "about_section")
@Getter
@Setter
@NoArgsConstructor
public class AboutSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String yearsExperience;
    private String projectsCompleted;
    private String techDebtReduced;
}
