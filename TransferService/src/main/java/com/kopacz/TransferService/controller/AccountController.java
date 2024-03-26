package com.kopacz.TransferService.controller;

import com.kopacz.TransferService.model.command.TransferCommand;
import com.kopacz.TransferService.model.dto.TransferDto;
import com.kopacz.TransferService.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferCommand transferCommand,
                                                Principal principal){
        return ResponseEntity.ok(accountService.validateTransfer(transferCommand, principal.getName()));
    }
}
