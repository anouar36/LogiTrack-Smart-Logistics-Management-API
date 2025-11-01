package com.logitrack.logitrack.exception;


public class UserAlreadyExistsException  extends RuntimeException{
    public UserAlreadyExistsException(String message){
        super(message);
    }
}
