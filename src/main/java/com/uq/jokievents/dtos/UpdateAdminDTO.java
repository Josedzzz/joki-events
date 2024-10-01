package com.uq.jokievents.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public record UpdateAdminDTO(
        @Null(message = "Username cannot be null")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String username,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email
) {}

