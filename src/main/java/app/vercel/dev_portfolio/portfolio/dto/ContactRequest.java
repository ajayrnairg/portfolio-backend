package app.vercel.dev_portfolio.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        String subject,

        @NotBlank(message = "Message cannot be empty")
        @Size(min = 10, message = "Message must be at least 10 characters")
        String message
) {}
