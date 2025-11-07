package com.logitrack.logitrack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This annotation tells Spring to return a 400 BAD REQUEST status
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}
