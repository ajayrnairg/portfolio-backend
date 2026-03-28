package app.vercel.dev_portfolio.portfolio.integration.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration")
@ActiveProfiles("test") // This matches 'application-test.yml'
public abstract class BaseIntegrationTest {
}