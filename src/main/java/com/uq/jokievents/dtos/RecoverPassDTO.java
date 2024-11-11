package com.uq.jokievents.dtos;

import jakarta.validation.constraints.*;

public record RecoverPassDTO(
        @NotBlank(message = "Email/Username cannot be blank")
        @Email(message = "Provide an actual mail")
        String email,

        @NotBlank(message = "Verification code cannot be blank")
        @Pattern(regexp = "\\d{6}", message = "Verification code must be exactly 6 digits")
        String verificationCode,

        @NotBlank(message = "New password cannot be blank")
        @Size(min = 4, message = "New password must be at least 4 characters long")
        String newPassword
) {}

