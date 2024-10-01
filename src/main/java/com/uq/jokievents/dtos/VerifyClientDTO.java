package com.uq.jokievents.dtos;

import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

/*
 * ¿Fue o no fue útil? Sí fue útil
 */
public record VerifyClientDTO(
        @Null(message = "Verification code cannot be empty")
        @Pattern(regexp = "\\d{6}", message = "Verification code must be exactly 6 digits")
        String verificationCode
) {}
