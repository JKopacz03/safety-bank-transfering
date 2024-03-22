package com.kopacz.TransferService.exceptions;

public class TransferConstraintsException extends RuntimeException {
    public TransferConstraintsException() {
    }

    public TransferConstraintsException(String message) {
        super(message);
    }
}
