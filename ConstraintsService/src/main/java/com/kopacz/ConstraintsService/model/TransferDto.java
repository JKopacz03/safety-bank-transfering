package com.kopacz.ConstraintsService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferDto {
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private Currency currency;
    private String mess;
}
