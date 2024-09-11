package com.uq.jokievents.dtos;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record UpdateClientDTO(
        @NotBlank(message = "ID card cannot be blank")
        @Size(min = 6, max = 12, message = "ID card must be between 6 and 12 characters")
        String idCard,

        @NotBlank(message = "Phone cannot be blank")
        @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
        String phone,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        String direction
) {}

