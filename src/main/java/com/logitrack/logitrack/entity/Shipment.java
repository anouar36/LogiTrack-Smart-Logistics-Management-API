package com.logitrack.logitrack.entity;

import com.logitrack.logitrack.entity.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
    
    private String trackingNumber;
    private Instant shippedAt;
    private Instant deliveredAt;
    
    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    
    @ManyToOne
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;
}
