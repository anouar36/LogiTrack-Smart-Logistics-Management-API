
package com.logitrack.logitrack.entity;

import com.logitrack.logitrack.entity.enums.POStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private POStatus status;
    
    private Instant createdAt = Instant.now();
    
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<PurchaseOrderLine> lines;
}
