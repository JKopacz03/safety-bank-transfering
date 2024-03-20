package com.kopacz.TransferService.service;

import com.kopacz.TransferService.exceptions.NotAccessToThisAccountException;
import com.kopacz.TransferService.exceptions.NotEfficientFundsException;
import com.kopacz.TransferService.exceptions.NotFoundAccountException;
import com.kopacz.TransferService.exceptions.TransferToAccountWithAnotherCurrencyException;
import com.kopacz.TransferService.model.Account;
import com.kopacz.TransferService.model.Transaction;
import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.dto.TransferDto;
import com.kopacz.TransferService.model.enums.TransactionType;
import com.kopacz.TransferService.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserService userService;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    @Transactional
    public TransferDto transfer(TransferCommand transferCommand, String username){
        /** TODO
         * RABBIT
         * Wywlapac bledy w glbolaqexhgandler
         * testy
         * */

        BigDecimal amount = transferCommand.getAmount();
        Account accountFrom = accountRepository.findByAccountNumber(transferCommand.getFromAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account from not exist"));

        validateAccountFrom(username, accountFrom, amount);

        Account accountTo = accountRepository.findByAccountNumber(transferCommand.getFromAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account to not exist"));

        if(!accountFrom.getCurrency().equals(accountTo.getCurrency())){
            throw new TransferToAccountWithAnotherCurrencyException();
        } //TODO  to bym dal do serwisu constraints

        //TODO to od krolika

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));

        saveTransactions(amount, accountFrom, accountTo);

        return new TransferDto(
            String.format("Transfer %f to %d end successfully", amount, accountTo.getAccountNumber())
        );
    }

    private void saveTransactions(BigDecimal amount, Account accountFrom, Account accountTo) {
        transactionService.save(new Transaction(
                TransactionType.SEND,
                amount,
                accountFrom
        ));

        transactionService.save(new Transaction(
                TransactionType.RECEIVE,
                amount,
                accountTo
        ));
    }

    private static void validateAccountFrom(String username, Account accountFrom, BigDecimal amount) {
        if(!accountFrom.getUser().getUsername().equals(username)){
            throw new NotAccessToThisAccountException();
        }

        if(accountFrom.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0){
            throw new NotEfficientFundsException();
        }
    }

}
