package com.uq.jokievents.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import lombok.Data;

public record UpdateAdminDTO(
        @Null(message = "Username cannot be null")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String username,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email
) {}

