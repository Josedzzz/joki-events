package com.uq.jokievents.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO(
        @NotBlank(message = "Email can not be blank")
        @Email(message = "Must be an em@il")
        String email
) {
}
