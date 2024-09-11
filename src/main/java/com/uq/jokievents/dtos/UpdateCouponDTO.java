package com.uq.jokievents.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateCouponDTO(
        @NotNull(message = "Discount cannot be null")
        @Min(value = 0, message = "Discount must be greater than or equal to 0")
        @Max(value = 100, message = "Discount must be less than or equal to 100")
        double discount,

        @NotNull(message = "Expiration date cannot be null")
        LocalDateTime expirationDate,

        @NotNull(message = "Minimum purchase amount cannot be null")
        @Min(value = 0, message = "Minimum purchase amount must be greater than or equal to 0")
        double minPurchaseAmount
) {}

