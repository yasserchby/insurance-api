package com.insurance.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractCostUpdateDTO {

    @NotNull(message = "Cost amount is required")
    @Positive(message = "Cost amount must be positive")
    private BigDecimal costAmount;
}