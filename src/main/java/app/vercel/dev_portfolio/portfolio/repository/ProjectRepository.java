package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Standard CRUD methods are inherited from JpaRepository
}