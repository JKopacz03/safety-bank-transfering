package com.kopacz.TransferService.service;

import com.kopacz.TransferService.exceptions.*;
import com.kopacz.TransferService.model.Account;
import com.kopacz.TransferService.model.Transaction;
import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.dto.TransferConstraintsDto;
import com.kopacz.TransferService.model.dto.TransferDto;
import com.kopacz.TransferService.model.enums.Currency;
import com.kopacz.TransferService.model.enums.TransactionType;
import com.kopacz.TransferService.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public TransferDto transfer(TransferCommand transferCommand, String username){

        BigDecimal amount = transferCommand.getAmount();

        Account accountFrom = accountRepository.findByAccountNumber(transferCommand.getFromAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account from not exist"));

        validateAccountFrom(username, accountFrom, amount);
        validateAccountsNumbers(transferCommand);

        Account accountTo = accountRepository.findByAccountNumber(transferCommand.getToAccount())
                .orElseThrow(() -> new NotFoundAccountException("Account to not exist"));

        validateCurrenciesOfAccount(accountFrom, accountTo);
        validateConstraints(transferCommand, accountFrom.getCurrency());

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));

        saveTransactions(amount, accountFrom, accountTo);

        return new TransferDto(
            String.format("Transfer %f to %d end successfully", amount, accountTo.getAccountNumber())
        );
    }

    private static void validateAccountsNumbers(TransferCommand transferCommand) {
        if(Objects.equals(transferCommand.getFromAccount(), transferCommand.getToAccount())){
            throw new TransferToSameAccountException("Cannot transfer funds to same account");
        }
    }

    private static void validateCurrenciesOfAccount(Account accountFrom, Account accountTo) {
        if(!accountFrom.getCurrency().equals(accountTo.getCurrency())){
            throw new TransferToAccountWithAnotherCurrencyException("Cannot transfer funds to account with another currency");
        }
    }

    private void validateConstraints(TransferCommand transferCommand, Currency currency) {
        Object receive = rabbitTemplate.convertSendAndReceive("constraints-queue",
                new TransferConstraintsDto(transferCommand.getAmount(), currency));
        String validationResult = (String) receive;
        if (validationResult.equals("TRANSFER_IN_WEEKEND_CONSTRAINT")) {
            throw new TransferConstraintsException("Transfers not allowed on weekends");
        }
        if (validationResult.equals("MAX15K_EURO_CONSTRAINT")) {
            throw new TransferConstraintsException("Transfers not allowed for amounts greater than 15K");
        }
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
            throw new NotAccessToThisAccountException("You don't have access to this account!");
        }

        if(accountFrom.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0){
            throw new NotEfficientFundsException("Not efficient funds!");
        }
    }

}
