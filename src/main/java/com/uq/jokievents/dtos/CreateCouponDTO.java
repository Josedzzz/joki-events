package com.uq.jokievents.dtos;

import java.time.LocalDateTime;

import javax.validation.constraints.*;

import lombok.Data;

@Data
public class CreateCouponDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Discount cannot be null")
    @Min(value = 0, message = "Discount must be greater than or equal to 0")
    @Max(value = 100, message = "Discount must be less than or equal to 100")
    private Double discount;

    @NotNull(message = "Expiration date cannot be null")
    private LocalDateTime expirationDate;

    @NotNull(message = "Minimum purchase amount cannot be null")
    @Min(value = 0, message = "Minimum purchase amount must be greater than or equal to 0")
    private Double minPurchaseAmount;
}
