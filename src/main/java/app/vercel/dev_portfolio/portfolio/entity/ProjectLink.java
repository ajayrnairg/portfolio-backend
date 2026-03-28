package app.vercel.dev_portfolio.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_links")
@Getter
@Setter
@NoArgsConstructor
public class ProjectLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String label;
    private String iconName;
    private String url;
}