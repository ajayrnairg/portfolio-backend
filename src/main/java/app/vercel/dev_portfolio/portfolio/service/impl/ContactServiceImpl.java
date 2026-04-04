package app.vercel.dev_portfolio.portfolio.service.impl;

import app.vercel.dev_portfolio.portfolio.dto.ContactRequest;
import app.vercel.dev_portfolio.portfolio.entity.ContactMessage;
import app.vercel.dev_portfolio.portfolio.repository.ContactRepository;
import app.vercel.dev_portfolio.portfolio.service.ContactService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;
    private final RestTemplate restTemplate = new RestTemplate(); // Built-in, no extra dependency

    @Value("${DISCORD_WEBHOOK_URL}")
    private String discordUrl;

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @Override
    public void processMessage(ContactRequest request, String ip) {
        // IP Rate Limiting: 3 per day
        Bucket ipBucket = ipBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofDays(1))))
                .build());

        if (!ipBucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Slow down! IP limit exceeded.");
        }

        // 1. Save to DB for history
        repository.save(ContactMessage.builder()
                .name(request.name())
                .email(request.email())
                .subject(request.subject())
                .message(request.message())
                .ipAddress(ip)
                .createdAt(LocalDateTime.now())
                .build());

        // 2. Send instant alert via Discord
        sendDiscordNotification(request);
    }

    @Async
    public void sendDiscordNotification(ContactRequest request) {
        // Formatting a "Rich Embed" for Discord
        Map<String, Object> payload = Map.of(
                "embeds", List.of(Map.of(
                        "title", "📬 New Portfolio Message: " + request.subject(),
                        "color", 5814783, // Nice Blurple color
                        "fields", List.of(
                                Map.of("name", "From", "value", request.name(), "inline", true),
                                Map.of("name", "Email", "value", request.email(), "inline", true),
                                Map.of("name", "Message", "value", request.message())
                        ),
                        "footer", Map.of("text", "Sent from Portfolio Backend")
                ))
        );

        try {
            restTemplate.postForEntity(discordUrl, payload, String.class);
        } catch (Exception e) {
            // Log error but don't crash the user's request
            System.err.println("Failed to send Discord alert: " + e.getMessage());
        }
    }
}