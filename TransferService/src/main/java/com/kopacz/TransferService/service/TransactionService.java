package com.kopacz.TransferService.service;

import com.kopacz.TransferService.model.Transaction;
import com.kopacz.TransferService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    public void save(Transaction transaction){
        repository.save(transaction);
    }

}
