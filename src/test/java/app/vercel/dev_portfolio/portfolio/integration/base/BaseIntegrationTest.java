package app.vercel.dev_portfolio.portfolio.integration.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all Integration Tests.
 * - Uses RANDOM_PORT to avoid port conflicts in CI/CD.
 * - Excludes MailAutoConfig to prevent trying to connect to real SMTP.
 * - Marks as @Transactional to rollback DB changes after every test.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
        }
)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
}