package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.entity.User;
import org.springframework.stereotype.Component;

@Component
public class LoginRequestMapper {    public  User toEntity(LoginRequest loginRequest){
        if(loginRequest ==  null) return null;
        User user = new User();
        user.setEmail(loginRequest.getUsername());
        user.setPasswordHash(loginRequest.getPassword());
        return user;
    }

}
