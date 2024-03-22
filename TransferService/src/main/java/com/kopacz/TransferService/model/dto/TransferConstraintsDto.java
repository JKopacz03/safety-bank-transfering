package com.kopacz.TransferService.model.dto;

import com.kopacz.TransferService.model.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferConstraintsDto {
    private BigDecimal amount;
    private Currency currency;
}
