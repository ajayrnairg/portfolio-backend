package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
    @Query("SELECT sc FROM SkillCategory sc LEFT JOIN FETCH sc.skills")
    List<SkillCategory> findAllWithSkills();
}