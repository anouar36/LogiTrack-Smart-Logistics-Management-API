package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long quantityOnHand;
    private Long quantityReserved;
    private Instant lastUpdatedAt = Instant.now();
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    private List<InventoryMovement> movements;
}
