package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Auth.RegisterDto;
import com.logitrack.logitrack.dto.LoginRequest;
import com.logitrack.logitrack.dto.ResClientDTO;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.service.AuthService;
import jakarta.validation.Valid;
import jdk.jfr.Registered;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;   // <-- لازم final

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public ResClientDTO register(@RequestBody @Valid RegisterDto dto){
        return authService.register(dto);
    }
}
