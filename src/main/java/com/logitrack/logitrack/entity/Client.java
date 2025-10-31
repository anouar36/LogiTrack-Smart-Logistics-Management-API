package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;  // link to the User entity

    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<SalesOrder> salesOrders;
}
