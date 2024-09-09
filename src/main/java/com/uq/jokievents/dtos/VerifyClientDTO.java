package com.uq.jokievents.dtos;

import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import lombok.Data;

/*
 * ¿Fue o no fue útil?
 */
@Data
public class VerifyClientDTO {
    
    @Null(message = "Verification code can not be empty")
    @Pattern(regexp = "\\d{6}", message = "Verification code must be exactly 6 digits")
    private String verificationCode;
}
