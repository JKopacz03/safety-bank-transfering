package com.kopacz.TransferService.exceptions;

public class NotFoundAccountException extends RuntimeException {
    public NotFoundAccountException() {
        super();
    }

    public NotFoundAccountException(String message) {
        super(message);
    }
}
