package com.logitrack.logitrack.dto;

import com.logitrack.logitrack.dto.User.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResClientDTO {
    private Long id;
    private String name;
    private UserResponseDTO user;
}