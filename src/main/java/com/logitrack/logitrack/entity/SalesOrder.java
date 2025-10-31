package com.logitrack.logitrack.entity;

import com.logitrack.logitrack.entity.enums.SOStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private SOStatus status;
    
    private Instant createdAt = Instant.now();
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<SalesOrderLine> lines;
    
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<Shipment> shipments;
}
