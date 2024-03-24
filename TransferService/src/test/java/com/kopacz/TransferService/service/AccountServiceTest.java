package com.kopacz.TransferService.service;
import com.kopacz.TransferService.exceptions.*;
import com.kopacz.TransferService.model.Account;
import com.kopacz.TransferService.model.Transaction;
import com.kopacz.TransferService.model.User;
import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.dto.TransferConstraintsDto;
import com.kopacz.TransferService.model.enums.AccountType;
import com.kopacz.TransferService.model.enums.Currency;
import com.kopacz.TransferService.model.enums.UserRole;
import com.kopacz.TransferService.repository.AccountRepository;
import com.kopacz.TransferService.service.AccountService;
import com.kopacz.TransferService.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private AccountService accountService;

    @Test
    void transfer_transferringFunds_fundsTransferred(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        Account accountTo = new Account(1L, 1001L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findByAccountNumber(1001L)).thenReturn(Optional.of(accountTo));
        when(rabbitTemplate.convertSendAndReceive("constraints-queue",new TransferConstraintsDto(transferCommand.getAmount(), Currency.PLN)))
                .thenReturn("OK");
        //when
        accountService.transfer(transferCommand,"user1");
        //then
        Assertions.assertEquals(BigDecimal.valueOf(900), accountFrom.getBalance());
        Assertions.assertEquals(BigDecimal.valueOf(1100), accountTo.getBalance());
        verify(transactionService, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_accountFromDontExist_throwNotFoundAccountException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        //when/then
        Assertions.assertThrows(
                NotFoundAccountException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_accountToDontExist_throwNotFoundAccountException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountTo = new Account(1L, 1001L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountTo));
        //when/then
        Assertions.assertThrows(
                NotFoundAccountException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_badUser_throwNotAccessToThisAccountException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        //when/then
        Assertions.assertThrows(
                NotAccessToThisAccountException.class,
                () -> accountService.transfer(transferCommand, "user2"));
    }

    @Test
    void transfer_notEfficientFunds_throwNotEfficientFundsException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(10), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        //when/then
        Assertions.assertThrows(
                NotEfficientFundsException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_transferToSameAccount_throwTransferToSameAccountException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1000L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        //when/then
        Assertions.assertThrows(
                TransferToSameAccountException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_differentCurrenciesAccounts_throwTransferToAccountWithAnotherCurrencyException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        Account accountTo = new Account(1L, 1001L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.USD, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findByAccountNumber(1001L)).thenReturn(Optional.of(accountTo));
        //when/then
        Assertions.assertThrows(
                TransferToAccountWithAnotherCurrencyException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_transferInWeekend_throwTransferConstraintsException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        Account accountTo = new Account(1L, 1001L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findByAccountNumber(1001L)).thenReturn(Optional.of(accountTo));
        when(rabbitTemplate.convertSendAndReceive("constraints-queue",new TransferConstraintsDto(transferCommand.getAmount(), Currency.PLN)))
                .thenReturn("TRANSFER_IN_WEEKEND_CONSTRAINT");
        //when/then
        Assertions.assertThrows(
                TransferConstraintsException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

    @Test
    void transfer_transferAbove15kEuro_throwTransferConstraintsException(){
        //given
        TransferCommand transferCommand = new TransferCommand(BigDecimal.valueOf(100), 1000L, 1001L);
        User user1 = new User(1L, "user1", "qwerty", UserRole.USER, Collections.emptySet());
        Account accountFrom = new Account(1L, 1000L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        Account accountTo = new Account(1L, 1001L, AccountType.PERSONAL, BigDecimal.valueOf(1000), Currency.PLN, Collections.emptySet(), user1, 0);
        when(accountRepository.findByAccountNumber(1000L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findByAccountNumber(1001L)).thenReturn(Optional.of(accountTo));
        when(rabbitTemplate.convertSendAndReceive("constraints-queue",new TransferConstraintsDto(transferCommand.getAmount(), Currency.PLN)))
                .thenReturn("MAX15K_EURO_CONSTRAINT");
        //when/then
        Assertions.assertThrows(
                TransferConstraintsException.class,
                () -> accountService.transfer(transferCommand, "user1"));
    }

}
