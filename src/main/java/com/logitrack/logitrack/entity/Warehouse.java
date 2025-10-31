package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String code;
    private String name;
    
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Inventory> inventories;
}
