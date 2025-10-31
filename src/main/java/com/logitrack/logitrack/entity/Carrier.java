package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private boolean active = true;
    
    @OneToMany(mappedBy = "carrier", cascade = CascadeType.ALL)
    private List<Shipment> shipments;
}
