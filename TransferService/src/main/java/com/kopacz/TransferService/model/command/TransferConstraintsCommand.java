package com.kopacz.TransferService.model.command;

import com.kopacz.TransferService.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferConstraintsCommand {
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private Currency currency;
    private String mess;
}
