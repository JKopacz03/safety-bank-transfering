package com.kopacz.ConstraintsService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transfer {
    private BigDecimal amount;
    private Currency currency;
}
