package com.uq.jokievents.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterClientDTO(
        @NotBlank(message = "ID card can not be empty.")
        @Size(min = 6, max = 12, message = "ID card must be between 6 and 12 characters.")
        String idCard,

        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Size(max = 150, message = "Address can be maximum of 150 characters")
        String address,

        @NotBlank(message = "Phone cannot be blank")
        @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
        String phone,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 4, message = "Password must be at least 4 characters long")
        String password
) {}

