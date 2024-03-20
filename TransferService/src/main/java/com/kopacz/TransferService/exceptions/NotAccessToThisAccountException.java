package com.kopacz.TransferService.exceptions;

public class NotAccessToThisAccountException extends RuntimeException {
    public NotAccessToThisAccountException() {
        super();
    }

    public NotAccessToThisAccountException(String message) {
        super(message);
    }
}
