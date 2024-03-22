package com.kopacz.TransferService.model;

import com.kopacz.TransferService.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private BigDecimal amount;
    @ManyToOne(
            cascade={CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "account_id")
    private Account account;

    public Transaction(TransactionType type, BigDecimal amount, Account account) {
        this.transactionType = type;
        this.account = account;
        if(type.equals(TransactionType.SEND)){
            this.amount = amount.negate();
        } else {
            this.amount = amount;
        }
    }
}
