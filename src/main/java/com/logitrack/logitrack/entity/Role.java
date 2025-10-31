package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private RoleType name;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    enum RoleType {
        ADMIN,
        WAREHOUSE_MANAGER,
        CLIENT
    }
}

