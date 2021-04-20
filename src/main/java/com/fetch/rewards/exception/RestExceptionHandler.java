package com.fetch.rewards.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler
    public ResponseEntity handleException(InsufficientPointsException exception) {
        log.error("Insufficient funds to cover request.", exception);
        return new ResponseEntity<>("Insufficient funds to cover payment, no points will be spent", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
