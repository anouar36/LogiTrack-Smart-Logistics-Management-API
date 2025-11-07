package com.logitrack.logitrack.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.logitrack.logitrack.exception.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, String> errorRespens = new HashMap<>();
        errorRespens.put("message", ex.getMessage());
        return new ResponseEntity<>(errorRespens, HttpStatus.CONFLICT);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage()); // The message from our custom exception
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<?> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotExistsException.class)
    public ResponseEntity<Map<String, String>> handelProductExists(ProductNotExistsException ex){
        Map<String,String> errorRespens = new HashMap<>();
        errorRespens.put("message",ex.getMessage());
        return new ResponseEntity<>(errorRespens , HttpStatus.NOT_FOUND) ;
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String ,String>> handleGenericException(Exception ex){
        Map<String,String> errorRespens = new HashMap<>();
        errorRespens.put("message",ex.getMessage());
        return  new ResponseEntity<>(errorRespens, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

