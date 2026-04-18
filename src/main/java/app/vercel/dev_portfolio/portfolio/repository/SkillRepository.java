package app.vercel.dev_portfolio.portfolio.repository;

import app.vercel.dev_portfolio.portfolio.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
}
