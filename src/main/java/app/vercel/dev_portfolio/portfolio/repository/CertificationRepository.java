package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.Certifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certifications, Long> {
    // Standard CRUD methods are inherited from JpaRepository
}