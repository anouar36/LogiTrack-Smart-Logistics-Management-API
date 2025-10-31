package com.logitrack.logitrack.dto.Auth;

import com.logitrack.logitrack.entity.SalesOrder;
import com.logitrack.logitrack.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
@Data
@AllArgsConstructor
@Component
public class RegisterDto {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String passwordHash;
}


