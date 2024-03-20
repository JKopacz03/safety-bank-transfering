package com.kopacz.TransferService.repository;

import com.kopacz.TransferService.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
