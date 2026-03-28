package app.vercel.dev_portfolio.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration")
@ActiveProfiles("test")
class PortfolioApplicationTests {

    @Test
    void contextLoads() {
    }

}