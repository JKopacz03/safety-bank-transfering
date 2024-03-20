package com.kopacz.TransferService.exceptions;

public class TransferToAccountWithAnotherCurrencyException extends RuntimeException {
    public TransferToAccountWithAnotherCurrencyException() {
        super();
    }

    public TransferToAccountWithAnotherCurrencyException(String message) {
        super(message);
    }
}
