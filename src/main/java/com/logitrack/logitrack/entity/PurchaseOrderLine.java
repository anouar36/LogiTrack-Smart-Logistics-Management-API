package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long quantity;
    private BigDecimal unitPrice;
    
    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
