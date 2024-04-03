package com.kopacz.TransferService.service;

import com.kopacz.TransferService.exceptions.*;
import com.kopacz.TransferService.model.Account;
import com.kopacz.TransferService.model.Transaction;
import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.command.TransferConstraintsCommand;
import com.kopacz.TransferService.model.dto.TransferConstraintsDto;
import com.kopacz.TransferService.model.dto.TransferDto;
import com.kopacz.TransferService.model.enums.TransactionStatus;
import com.kopacz.TransferService.model.enums.TransactionType;
import com.kopacz.TransferService.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final RabbitTemplate rabbitTemplate;

    public String validateTransfer(TransferCommand command, String username) {
        Account accountFrom = accountRepository.findByAccountNumber(command.getFromAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account from not exist"));

        Account accountTo = accountRepository.findByAccountNumber(command.getToAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account to not exist"));

        if(!accountFrom.getUser().getUsername().equals(username)){
            throw new NotAccessToThisAccountException("You don't have access to this account!");
        }
        if(accountFrom.getBalance().subtract(command.getAmount()).compareTo(BigDecimal.ZERO) < 0){
            throw new NotEfficientFundsException("Not efficient funds!");
        }
        validateCurrenciesOfAccount(accountFrom, accountTo);
        validateAccountsNumbers(command);

        TransferConstraintsDto constraintsDto = new TransferConstraintsDto(command.getFromAccount(),
                command.getToAccount(),
                command.getAmount(),
                accountFrom.getCurrency());

        rabbitTemplate.convertAndSend("constraints-queue", constraintsDto);
        return "Your transfer was commissioned";
    }

    @RabbitListener(queues = "constraints-back-queue")
    @Transactional
    public void transfer(TransferConstraintsCommand constraintsCommand){
        BigDecimal amount = constraintsCommand.getAmount();

        Account accountFrom = accountRepository.findByAccountNumber(constraintsCommand.getFromAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account from not exist"));

        Account accountTo = accountRepository.findByAccountNumber(constraintsCommand.getToAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account to not exist"));
        try {
            validateConstraints(constraintsCommand.getMess());

            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            accountTo.setBalance(accountTo.getBalance().add(amount));

            saveTransactions(amount, accountFrom, accountTo);
        } catch(TransferConstraintsException e){
            saveFailedTransactions(amount, accountFrom, accountTo);
        }
    }

    @Transactional
    public static void transfer(Account from, Account to, BigDecimal amount){
        Account first = from;
        Account second = to;
        if (first.compareTo(second) < 0) {
            first = to;
            second = from;
        }
        synchronized(first){
            synchronized(second){
                from.setBalance(from.getBalance().subtract(amount));
                to.setBalance(to.getBalance().add(amount));
            }
        }
    }

    private static void validateAccountsNumbers(TransferCommand command) {
        if(Objects.equals(command.getFromAccount(), command.getToAccount())){
            throw new TransferToSameAccountException("Cannot transfer funds to same account");
        }
    }

    private static void validateCurrenciesOfAccount(Account accountFrom, Account accountTo) {
        if(!accountFrom.getCurrency().equals(accountTo.getCurrency())){
            throw new TransferToAccountWithAnotherCurrencyException("Cannot transfer funds to account with another currency");
        }
    }

    private void validateConstraints(String mess) {
        if (mess.equals("TRANSFER_IN_WEEKEND_CONSTRAINT")) {
            throw new TransferConstraintsException("Transfers not allowed on weekends");
        }
        if (mess.equals("MAX15K_EURO_CONSTRAINT")) {
            throw new TransferConstraintsException("Transfers not allowed for amounts greater than 15K");
        }
    }

    private void saveTransactions(BigDecimal amount, Account accountFrom, Account accountTo) {
        transactionService.save(new Transaction(
                TransactionType.SEND,
                amount,
                accountFrom,
                TransactionStatus.SUCCEED
        ));

        transactionService.save(new Transaction(
                TransactionType.RECEIVE,
                amount,
                accountTo,
                TransactionStatus.SUCCEED
        ));
    }

    private void saveFailedTransactions(BigDecimal amount, Account accountFrom, Account accountTo) {
        transactionService.save(new Transaction(
                TransactionType.SEND,
                amount,
                accountFrom,
                TransactionStatus.FAILED
        ));

        transactionService.save(new Transaction(
                TransactionType.RECEIVE,
                amount,
                accountTo,
                TransactionStatus.FAILED
        ));
    }



}
