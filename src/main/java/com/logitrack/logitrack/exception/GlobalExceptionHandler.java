package com.logitrack.logitrack.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

