package com.kopacz.ConstraintsService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferCommand {
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private Currency currency;
}
