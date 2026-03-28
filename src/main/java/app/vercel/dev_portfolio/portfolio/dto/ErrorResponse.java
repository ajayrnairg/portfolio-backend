package app.vercel.dev_portfolio.portfolio.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {}