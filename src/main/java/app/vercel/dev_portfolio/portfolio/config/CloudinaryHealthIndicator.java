package app.vercel.dev_portfolio.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class CloudinaryHealthIndicator implements HealthIndicator {

    @Value("${app.cloudinary.resume-url}")
    private String resumeUrl;

    @Override
    public Health health() {
        try {
            var connection = (HttpURLConnection) new URL(resumeUrl).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return Health.up().withDetail("status", responseCode).build();
            }
            return Health.down().withDetail("status", responseCode).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
