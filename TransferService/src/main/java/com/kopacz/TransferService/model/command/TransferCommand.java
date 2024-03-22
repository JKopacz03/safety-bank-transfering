package com.kopacz.TransferService.model.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferCommand {
    @DecimalMin(message = "missing amount", value = "0.0", inclusive = false)
    private BigDecimal amount;
    @Min(value = 1, message = "number of account from cannot be negative")
    private Long fromAccount;
    @Min(value = 1, message = "number of account to cannot be negative")
    private Long toAccount;
}
