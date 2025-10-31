package com.logitrack.logitrack.exception;

import com.logitrack.logitrack.entity.User;

public class UserAlreadyExistsException  extends RuntimeException{
    public UserAlreadyExistsException(String message){
        super(message);
    }
}
