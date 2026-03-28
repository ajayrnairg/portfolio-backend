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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String myEmail;

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // Global limit: 100 per day
    private final Bucket globalBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofDays(1))))
            .build();

    @Override
    public void processMessage(ContactRequest request, String ip) {
        // IP Limit: 3 per day
        Bucket ipBucket = ipBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofDays(1))))
                .build());

        if (!ipBucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "IP limit exceeded (3/day)");
        }

        if (!globalBucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Daily global limit reached");
        }

        repository.save(ContactMessage.builder()
                .name(request.name())
                .email(request.email())
                .subject(request.subject())
                .message(request.message())
                .ipAddress(ip)
                .createdAt(LocalDateTime.now())
                .build());

        sendEmailNotification(request);
    }

    @Async
    public void sendEmailNotification(ContactRequest request) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(myEmail);
        mail.setSubject("Portfolio Contact: " + request.subject());
        mail.setText("Name: " + request.name() + "\nEmail: " + request.email() + "\n\n" + request.message());
        mailSender.send(mail);
    }
}
