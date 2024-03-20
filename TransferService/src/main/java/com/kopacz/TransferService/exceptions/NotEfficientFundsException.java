package com.kopacz.TransferService.exceptions;

public class NotEfficientFundsException extends RuntimeException {
    public NotEfficientFundsException() {
        super();
    }

    public NotEfficientFundsException(String message) {
        super(message);
    }
}
