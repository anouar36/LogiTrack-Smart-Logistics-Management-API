package com.logitrack.logitrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {

    @Email(message = "Please make sure your email is correct.")
    private String email;
    @Size(min = 6, message = "Your password must be at least 6 characters long.")
    private String password;
}
