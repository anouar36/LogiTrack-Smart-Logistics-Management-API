package com.logitrack.logitrack.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserResponseDTO {

    private String email;
    private String passwordHash;
    private boolean active = true;
}
