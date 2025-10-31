package com.logitrack.logitrack.entity;

import com.logitrack.logitrack.entity.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private MovementType type;
    
    private Long quantity;
    private Instant occurredAt = Instant.now();
    private String referenceDoc;
    
    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;
}
