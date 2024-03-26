package com.kopacz.TransferService.model;

import com.kopacz.TransferService.model.enums.AccountType;
import com.kopacz.TransferService.model.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @OneToMany(
            mappedBy = "account"
    )
    private Set<Transaction> transactions = new HashSet<>();
    @ManyToOne(
            cascade={CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "user_id")
    private User user;
}
