package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.WorkSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSectionRepository extends JpaRepository<WorkSection, Long> {
    // Standard CRUD methods are inherited from JpaRepository
}