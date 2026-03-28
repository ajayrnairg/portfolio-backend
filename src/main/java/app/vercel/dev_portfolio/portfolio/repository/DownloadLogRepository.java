package app.vercel.dev_portfolio.portfolio.repository;


import app.vercel.dev_portfolio.portfolio.entity.Profile;
import app.vercel.dev_portfolio.portfolio.entity.ResumeDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadLogRepository extends JpaRepository<ResumeDownloadLog, Long> {
    // Standard CRUD methods are inherited from JpaRepository
}