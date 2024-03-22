package com.kopacz.TransferService.exceptions;

public class TransferToSameAccountException extends RuntimeException {
    public TransferToSameAccountException() {
        super();
    }

    public TransferToSameAccountException(String message) {
        super(message);
    }
}
