package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String contact;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders;
}
