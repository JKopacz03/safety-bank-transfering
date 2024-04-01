package com.kopacz.TransferService.exceptions;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotAccessToThisAccountException.class)
    public ResponseEntity<ErrorMessage> notAccessToThisAccountException(NotAccessToThisAccountException ex, HttpServletRequest request) {
        return createErrorResponse(UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(TransferToSameAccountException.class)
    public ResponseEntity<ErrorMessage> transferToSameAccountException(TransferToSameAccountException ex, HttpServletRequest request) {
        return createErrorResponse(BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorMessage> optimisticLockException(OptimisticLockException ex, HttpServletRequest request) {
        return createErrorResponse(BAD_REQUEST, "Please try again a transaction", request);
    }

    @ExceptionHandler(TransferConstraintsException.class)
    public ResponseEntity<ErrorMessage> transferConstraintsException(TransferConstraintsException ex, HttpServletRequest request) {
        return createErrorResponse(BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NotEfficientFundsException.class)
    public ResponseEntity<ErrorMessage> notEfficientFundsException(NotEfficientFundsException ex, HttpServletRequest request) {
        return createErrorResponse(BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NotFoundAccountException.class)
    public ResponseEntity<ErrorMessage> notFoundAccountException(NotFoundAccountException ex, HttpServletRequest request) {
        return createErrorResponse(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(TransferToAccountWithAnotherCurrencyException.class)
    public ResponseEntity<ErrorMessage> transferToAccountWithAnotherCurrencyException(TransferToAccountWithAnotherCurrencyException ex, HttpServletRequest request) {
        return createErrorResponse(BAD_REQUEST, ex.getMessage(), request);
    }


    private ResponseEntity<ErrorMessage> createErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .timestamp(LocalDateTime.now())
                .code(status.value())
                .status(status.getReasonPhrase())
                .message(message)
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .timestamp(LocalDateTime.now())
                .code(UNAUTHORIZED.value())
                .status(UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), UNAUTHORIZED);
    }

}