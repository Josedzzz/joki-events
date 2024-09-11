package com.uq.jokievents.dtos;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CreateCouponDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotNull(message = "Discount cannot be null")
        @Min(value = 0, message = "Discount must be greater than or equal to 0")
        @Max(value = 100, message = "Discount must be less than or equal to 100")
        Double discount,

        @NotNull(message = "Expiration date cannot be null")
        LocalDateTime expirationDate,

        @NotNull(message = "Minimum purchase amount cannot be null")
        @Min(value = 0, message = "Minimum purchase amount must be greater than or equal to 0")
        Double minPurchaseAmount
) {}

