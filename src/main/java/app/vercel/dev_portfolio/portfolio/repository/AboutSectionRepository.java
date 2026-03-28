package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.AboutSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AboutSectionRepository extends JpaRepository<AboutSection, Long> {
    // Standard CRUD methods are inherited from JpaRepository
}