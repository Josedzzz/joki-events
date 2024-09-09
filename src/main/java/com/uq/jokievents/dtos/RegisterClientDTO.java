package com.uq.jokievents.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class RegisterClientDTO {

    @NotBlank(message = "ID card can not be empty.")
    @Size(min = 6, max = 12, message = "ID card must be between 6 and 12 characters.")
    private String idCard; 

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    private String address;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid") // Algún día entenderé Regex.
    private String phone;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid") // Piensan en todo los developers.
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;
}
