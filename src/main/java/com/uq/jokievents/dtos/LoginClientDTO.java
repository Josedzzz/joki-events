package com.uq.jokievents.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import lombok.Data;

public record LoginClientDTO(
        @Null(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 4, message = "Password must be at least 4 characters long")
        String password
) {}

