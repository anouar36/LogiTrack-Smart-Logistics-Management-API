package com.logitrack.logitrack.dto.Auth;

import com.logitrack.logitrack.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.management.relation.Role;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JwtUserData {

    private Long id ;
    private String email;
    private List<String> roles;

    public JwtUserData(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}
